package com.eleven.acoustic.receive.demodulate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.eleven.acoustic.receive.demodulate.decode.ConvolutionDecoder;
import com.eleven.acoustic.receive.demodulate.decode.GRAYDecoder;
import com.eleven.acoustic.util.Complex;
import com.eleven.acoustic.util.FFT;

import java.util.Arrays;


/**
 * Created by Eleven on 2016/4/15.
 */
public class OFDMSignalDemodulator extends SignalDemodulator {
    private int mState;

    private GRAYDecoder grayDecoder;
    private ConvolutionDecoder convolutionDecoder;

    private static final int SILENCE = 0;
    private static final int SIGNAL = 1;
    private static final int SIGNAL_RECEIVED = 2;
    private static final int SIGNAL_NOT_RECEIVED = 3;


    private static final int nfft_SYN = 256;

    private static final double PI = Math.PI;

    private static final int NFFT_DEMODULATE = 512;
    private static final int LEN_PRE = NFFT_DEMODULATE/2;
    private static final int LEN_SYM = NFFT_DEMODULATE + LEN_PRE;

    private static final int FRE_LOW = 96;
    private static final int FRE_HIGH = 99;

    private long demodulateTime;

    private static final double[] startSignal = creatLFMSignal(4000, 5000, 20, 44100);

    private static final double[] endSignal = creatLFMSignal(5000, 4000, 20, 44100);

    public static Handler mHandler;

    public OFDMSignalDemodulator() {
        this.grayDecoder = new GRAYDecoder();
        this.convolutionDecoder = new ConvolutionDecoder();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == SIGNAL_RECEIVED) {
                    long time = msg.getData().getLong("time");
                    receivedListener.onReceived(baseBand);
                }
                if (msg.what == SIGNAL_NOT_RECEIVED) {
                    receivedListener.onNotReceived();
                }
            }
        };
    }

    @Override
    public void doDemodulate(double[] data) {
        int signalStart = 0;
        int signalEnd = 0;
        if (mState == SILENCE) {
            double[] coffStart = new double[data.length - startSignal.length + 1];
            for (int i = 0; i < coffStart.length; i++) {
                double tmp = 0.0;
                for (int j = 0; j < startSignal.length; j++) {
                    tmp += startSignal[j] * data[i + j];
                }
                coffStart[i] = Math.abs(tmp);
            }
            int maxIndex = findMax(coffStart);

            if (isSignal(maxIndex, coffStart, data)) {
                mState = SIGNAL;
                signalStart = maxIndex + 2*startSignal.length;
            }
        }
        if (mState == SILENCE) {
            mState = SIGNAL_NOT_RECEIVED;
        } else if (mState == SIGNAL) {
            int index = signalStart;
            if (index < data.length - 2*endSignal.length) {
                double[] coffEnd = new double[data.length - endSignal.length - index + 1];
                for (int i = index; i < index + coffEnd.length; i++) {
                    double tmp = 0.0;
                    for (int j = 0; j < endSignal.length; j++) {
                        tmp += endSignal[j] * data[i + j];
                    }
                    coffEnd[i - index] = Math.abs(tmp);
                }
                int maxIndex = findMax(coffEnd);

                if (maxIndex < data.length - index - nfft_SYN) {
                    if (isSignalEnd(index, maxIndex, coffEnd, data)) {
                        mState = SIGNAL_RECEIVED;
                        signalEnd = index + maxIndex - endSignal.length;
                    }
                }
            }
        }

        if (mState == SIGNAL) {
            mState = SIGNAL_NOT_RECEIVED;
        } else if (mState == SIGNAL_RECEIVED) {
            long startTime = System.currentTimeMillis();
            baseBand = processData(data, signalStart, signalEnd);
            long endTime = System.currentTimeMillis();
            demodulateTime = endTime - startTime;
            if (baseBand != null)
                sendMessage(SIGNAL_RECEIVED);
            mState = SILENCE;
        }
        if (mState == SIGNAL_NOT_RECEIVED) {
            sendMessage(SIGNAL_NOT_RECEIVED);
            mState = SILENCE;
        }
    }

    private  boolean isSignal(int soundStart, double[] coefdata, double[] data) {
        double[] fftData = doRFFT(data, soundStart + startSignal.length* 2 + LEN_PRE, NFFT_DEMODULATE, NFFT_DEMODULATE);
        return isFrequenceRight(fftData);
    }

    private boolean isSignalEnd(int soundStart, int offset, double[] coefdata, double[] data) {
        double[] fftData = doRFFT(data, soundStart + offset, nfft_SYN, nfft_SYN);
        return !isFrequenceRight(fftData);
    }

    private int[] processData(double[] signal, int startIndex, int endIndex) {
        int symNums = (int) Math.round((endIndex-startIndex-NFFT_DEMODULATE/4)/(1.5*NFFT_DEMODULATE));
        double[] phaseDiffs = new double[4*(symNums-1)];
        int[] binaryCodes = new int[8*(symNums-1)];
        startIndex = startIndex + LEN_PRE - 30;
        Complex[] comPre = doCFFT(signal, startIndex, NFFT_DEMODULATE, NFFT_DEMODULATE);
        for (int i = 1; i < symNums; i++) {
            Complex[] comPost = doCFFT(signal, startIndex + i*LEN_SYM, NFFT_DEMODULATE, NFFT_DEMODULATE);
            for (int j = FRE_LOW; j <= FRE_HIGH; j++) {
                double phase_diff = mod(angle(comPost[j]) - angle(comPre[j]), 2*PI);
                //double phase_diff = angle(comPost[j]) - angle(comPre[j]);
                if (phase_diff < 2*PI && phase_diff > 7*PI/4) {
                    phase_diff = 2*PI - phase_diff;
                }
                phaseDiffs[4*(i-1) + j - FRE_LOW] = phase_diff;
            }
            comPre = comPost;
        }
        for (int i =0; i < phaseDiffs.length; i++) {
            if (phaseDiffs[i] < PI/4) {
                binaryCodes[2*i] = 1;
                binaryCodes[2*i + 1] = 1;
            } else if (phaseDiffs[i] >= PI/4 && phaseDiffs[i] < 3*PI/4) {
                binaryCodes[2*i] = 0;
                binaryCodes[2*i + 1] = 1;
            } else if (phaseDiffs[i] >= 3*PI/4 && phaseDiffs[i] < 5*PI/4) {
                binaryCodes[2*i] = 0;
                binaryCodes[2*i + 1] = 0;
            } else if (phaseDiffs[i] >= 5*PI/4 && phaseDiffs[i] < 7*PI/4) {
                binaryCodes[2*i] = 1;
                binaryCodes[2*i + 1] = 0;
            }
        }
        int[] grayCodes = convolutionDecoder.decode(Arrays.copyOfRange(binaryCodes, 0, binaryCodes.length - 4));
        return grayDecoder.decode(Arrays.copyOfRange(grayCodes, 0, grayCodes.length - 2));
    }

    private static double[] doRFFT(double[] data, int start, int offset, int fftLength) {
        double[] sub = Arrays.copyOfRange(data, start, start + offset);
        return FFT.rFFT(sub, fftLength);
    }

    private static Complex[] doCFFT(double[] data, int start, int offset, int fftLength) {
        double[] sub = Arrays.copyOfRange(data, start, start + offset);
        return FFT.cFFT(sub, fftLength);
    }

    private static boolean isFrequenceRight(double[] fftData) {
        int index = findMax(fftData);
        if ((index >= 96 && index <=99) || (index >= 413 && index <= 416)) {
            return true;
        } else {
            return false;
        }
    }

    private static int findMax(double[] data) {
        int maxIndex = 0;
        double max = data[maxIndex];
        for (int i = 0; i < data.length; i++){
            if (data[i] > max) {
                max = data[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static double[] creatLFMSignal(int startFre, int endFre, int timeMs, int sampleRate) {
        int signalLength = timeMs*sampleRate/1000;
        double[] signal = new double[signalLength];
        double freInterval = ((double)(endFre - startFre)) / (signalLength - 1);

        for (int i = 0; i < signalLength; i++) {
            double fre = startFre + i*freInterval;
            signal[i] = Math.cos(2*Math.PI*fre*i/sampleRate);
        }
        return signal;
    }


    private void sendMessage(int messageType) {
        Message msg = Message.obtain();
        msg.what = messageType;
        if (messageType == SIGNAL_RECEIVED) {
            Bundle bundle = new Bundle();
            bundle.putLong("time", demodulateTime);
            msg.setData(bundle);
        }
        mHandler.sendMessage(msg);
    }

    private static double angle(Complex com) {
        return Math.atan2(com.getImage(), com.getReal());
    }

    private static double mod(double x, double y) {
        double c = Math.floor(x / y);
        double r = x - c*y;
        return r;
    }
}
