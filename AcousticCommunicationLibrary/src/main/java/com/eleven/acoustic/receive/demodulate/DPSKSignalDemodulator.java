package com.eleven.acoustic.receive.demodulate;

import android.os.Handler;
import android.os.Message;

import com.eleven.acoustic.receive.demodulate.decode.Decoder;
import com.eleven.acoustic.receive.demodulate.decode.GRAYDecoder;
import com.eleven.acoustic.util.FFT;

import java.util.Arrays;


/**
 * Created by Eleven on 2016/4/13.
 */
public class DPSKSignalDemodulator extends SignalDemodulator {
    private int mState;

    private static final int nfft = 256;//fft长度

    private static final int m = 21;
    private static final double WC = 2*Math.PI*5000;
    private static final double Ts = 1.0/44100;

    private Decoder decoder;

    private long demodulateTime;

    private static final int SILENCE = 0;
    private static final int SIGNAL = 1;
    private static final int SIGNAL_RECEIVED = 2;
    private static final int SIGNAL_NOT_RECEIVED = 3;

    private static final int PEAK = 29; //5000Hz对应频率点

    private static final double[] startSignal = creatLFMSignal(4000, 6000, 20, 44100);

    private static final double[] endSignal = creatLFMSignal(6000, 4000, 20, 44100);

    public static Handler mHandler;

    public DPSKSignalDemodulator() {
        decoder = new GRAYDecoder();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == SIGNAL_RECEIVED) {
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

                if (maxIndex < data.length - index - nfft) {
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

    private boolean isSignal(int soundStart, double[] coefdata, double[] data) {
        double[] fftData = doFFT(data, soundStart + 2*startSignal.length, nfft, nfft);
        normalizeFftData(fftData);
        return isFrequenceRight(fftData);
    }

    private boolean isSignalEnd(int soundStart, int offset, double[] coefdata, double[] data) {
        double[] fftData = doFFT(data, soundStart + offset, nfft, nfft);
        normalizeFftData(fftData);
        return !isFrequenceRight(fftData);
    }

    private  void normalizeFftData(double[] fftData) {
        int index = findMax(fftData);
        double max = fftData[index];
        for (int i = 0; i < fftData.length; i++) {
            fftData[i] /= max;
        }
    }

    private static double[] doFFT(double[] data, int start, int offset, int fftLength) {
        double[] sub = Arrays.copyOfRange(data, start, start + offset);
        return FFT.rFFT(sub, fftLength);
    }

    private static boolean isFrequenceRight(double[] fftData) {
        double fre = fftData[PEAK];
        if (fre > 0.8)
            return true;
        else
            return false;
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

    private int[] processData(double[] signal, int startIndex, int endIndex) {
        int length = endIndex - startIndex + 1;
        int codeNums = (int) Math.round(((double)length)/441);
        double[] phaseMid = new double[codeNums];

        for (int i = 0; i < codeNums; i++){
            int index = startIndex + i*441 + 220 - (m-1)/2;
            double a = 0.0;
            double b = 0.0;
            for (int k = 0; k < m; k++){
                a += Math.cos(WC*Ts*(k - (m-1)/2))*signal[index + k];
                b += Math.sin(WC*Ts*(k - (m-1)/2))*signal[index + k];
            }
            double c = 1 - Math.sin(m*WC*Ts)/(m*Math.sin(WC*Ts));
            double d = 1 + Math.sin(m*WC*Ts)/(m*Math.sin(WC*Ts));
            phaseMid[i] = Math.atan2(a * c, b * d);
        }

        double[] phaseDiff = new double[phaseMid.length - 1];
        for (int i = 0; i < phaseDiff.length; i++){
            phaseDiff[i] = mod(phaseMid[i + 1]-phaseMid[i], (2*Math.PI));
        }
        int[] binaryResult = new int[phaseDiff.length];
        for (int i = 0; i < binaryResult.length; i++){
            if ((phaseDiff[i] >= 0.0 && phaseDiff[i] < Math.PI/2) || (phaseDiff[i] < 2*Math.PI&& phaseDiff[i] > 3*Math.PI/2)) {
                binaryResult[i] = 0;
            } else {
                binaryResult[i] = 1;
            }
        }
        return decoder.decode(binaryResult);
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

    private static double mod(double x, double y) {
        double result = Math.abs(x) % Math.abs(y);
        int quo = (int) Math.floor(Math.abs(x) / Math.abs(y));
        if (result != 0.0)
            result = Math.abs(y)*(quo + 1) - result;
        return result;
    }

    private void sendMessage(int messageType) {
        Message msg = Message.obtain();
        msg.what = messageType;
        mHandler.sendMessage(msg);
    }
}
