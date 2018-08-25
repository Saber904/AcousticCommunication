package com.eleven.acoustic.send.generate;

import android.util.Log;

import com.eleven.acoustic.receive.demodulate.decode.ConvolutionDecoder;
import com.eleven.acoustic.receive.demodulate.decode.GRAYDecoder;
import com.eleven.acoustic.send.generate.encode.ConvolutionEncoder;
import com.eleven.acoustic.send.generate.encode.Encoder;
import com.eleven.acoustic.send.generate.encode.GrayEncoder;
import com.eleven.acoustic.util.Complex;
import com.eleven.acoustic.util.FFT;

/**
 * Created by Eleven on 2016/1/22.
 */
public class OFDMSignalGenerator extends SignalGenerator {

    private int[] mRawData;

    private final Encoder encoder = new GrayEncoder();
    private final Encoder channelEncoder = new ConvolutionEncoder(2, 1 ,3);
    private final int SAMPLE_RATE = 44100;

    private ConvolutionDecoder convolutionDecoder = new ConvolutionDecoder();
    private GRAYDecoder grayDecoder = new GRAYDecoder();

    private final double FREQUENCE_LOW = 8200;
    private final double FREQUENCE_HIGH = 8530;
    private final int FFT_N = 512;

    private final double FRE_INTERVAL = ((double)SAMPLE_RATE)/FFT_N;
    private final int INDEX_LOW = (int) Math.ceil(FREQUENCE_LOW/FRE_INTERVAL);
    private final int INDEX_HIGH = (int) Math.floor(FREQUENCE_HIGH / FRE_INTERVAL);

    private final double BETA = 0.25;

    private final double PI = Math.PI;

    private static short[] startSignal = creatLFMSignal(4000, 5000, 20, 44100);
    private static short[] endSignal = creatLFMSignal(5000, 4000, 20, 44100);

    @Override
    public short[] doGenerate(int[] data) {
        Log.i("SignalGenerate", "OFDM signal");

        mRawData = data;

        if (mRawData == null) {
            return null;
        }

        int codeLength = mRawData.length * 4;

        int[] binaryCodes = encoder.encoded(data);
        int[] sendCodes = channelEncoder.encoded(binaryCodes);


        double[] signal = creatSignal(sendCodes);

//        double fre_interval = ((double)SAMPLE_RATE)/FFT_N;
//        int index_low = (int) Math.ceil(FREQUENCE_LOW/fre_interval);
//        int index_high = (int) Math.floor(FREQUENCE_HIGH/fre_interval);
//        Complex[] X = new Complex[FFT_N];
//
//        for (int i = 0; i <= FFT_N/2; i++) {
//            if (i <= index_high && i >= index_low) {
//                X[i] = new Complex(1.0,1.0);
//            } else {
//                X[i] = new Complex(0.0,0.0);
//            }
//        }
//        hermitian(X);
//        Complex[] signal_complx = FFT.iFFT(X);
//        int len_pre = (int) 0.5*FFT_N;
//        //int len_pre = 0;
//        int len_post = (int) BETA*FFT_N;
//        //int len_post = 0;
//        double[] signal_real = new double[len_pre + 2*FFT_N + len_post];
//        for (int i = 0; i < signal_real.length; i++) {
//            if (i < len_pre) {
//                signal_real[i] = signal_complx[FFT_N - len_pre + i].getReal();
//            }
//            if (i >= len_pre && i < len_pre + FFT_N) {
//                signal_real[i] = signal_complx[i-len_pre].getReal();
//            }
//            if (i >= len_pre + FFT_N && i < len_pre + 2*FFT_N) {
//                signal_real[i] = signal_complx[i - len_pre - FFT_N].getReal();
//            }
//            if (i >= len_pre + 2*FFT_N) {
//                signal_real[i] = signal_complx[i - len_pre - 2*FFT_N].getReal();
//            }
//        }
        //windowing(signal_real);
        //cTransform(signal_real);
        short[] signal_short = doublesToShorts(signal);

        short[] signal_final = new short[startSignal.length*5 + signal_short.length];

        //System.arraycopy(pureSignal, 0, signal_final, 0, startSignal.length);

        System.arraycopy(startSignal, 0, signal_final, startSignal.length, startSignal.length);

        System.arraycopy(signal_short, 0, signal_final, startSignal.length*3, signal_short.length);

        System.arraycopy(endSignal, 0, signal_final, signal_final.length-endSignal.length, endSignal.length);

        return signal_final;
    }

    private double[] creatSignal(int[] binaryCodes) {
        int num = binaryCodes.length/8;
        int len_pre = FFT_N/2;
        int len_post = FFT_N/4;

        int len_signal = (len_pre+FFT_N)*(num+2) + len_post;

        double[] signal = new double[len_signal];

        int[] DQPSK = generateDQPSK(binaryCodes);

        for (int i = 0; i < num+2; i++) {
            Complex[] X = new Complex[FFT_N];

            for (int j = 0; j <= FFT_N/2; j++) {
                if (j <= INDEX_HIGH && j >= INDEX_LOW) {
                    X[j] = Complex.QPSK[DQPSK[4*i + j - INDEX_LOW]];
                } else {
                    X[j] = Complex.ZERO;
                }
            }
            hermitian(X);
            Complex[] signal_complx = FFT.iFFT(X);
            double[] signal_real = new double[len_pre + FFT_N + len_post];
            for (int k = 0; k < signal_real.length; k++) {
                if (k < len_pre) {
                    signal_real[k] = signal_complx[FFT_N-len_pre+k].getReal();
                }
                if (k >= len_pre && k < len_pre + FFT_N) {
                    signal_real[k] = signal_complx[k-len_pre].getReal();
                }
                if (k >= len_pre + FFT_N) {
                    signal_real[k] = signal_complx[k-len_pre-FFT_N].getReal();
                }
            }
            windowing(signal_real);
            int startIndex = (len_pre + FFT_N)*i;
            for (int n = startIndex; n < startIndex + signal_real.length; n++) {
                signal[n] += signal_real[n - startIndex];
            }
        }
        return signal;

    }

    private void cTransform(double[] data) {
        double V = 0.0;
        double u = 3;
        for (double a:data) {
            V += Math.abs(a);
        }
        V /= data.length;
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0.0)
                data[i] = V*data[i]*Math.log(1 + u*Math.abs(data[i])/V)/(Math.log(1 + u)*Math.abs(data[i]));
            else
                data[i] = 0.0;
        }
    }

    private int[] generateDQPSK(int[] binaryCodes) {
        int length = 4*(binaryCodes.length/8+2);
        int[] DQPSK = new int[length];
        for (int i=0; i < 4; i++) {
            DQPSK[i]=i;
        }
        for (int i = 1; i < binaryCodes.length/8+2; i++) {
            for (int j = 0; j < 4; j++) {
                int num;
                if (8*(i-1)+2*j < binaryCodes.length)
                    num = binaryCodes[8*(i-1)+2*j]*2+binaryCodes[8*(i-1)+2*j+1];
                else
                    num = 3;

                if (num == 3)
                    num = 0;
                else if (num == 0)
                    num = 2;
                else if (num == 2)
                    num = 3;

                DQPSK[4*i+j] = (DQPSK[4*i+j-4]+num)%4;

            }
        }
        return DQPSK;
    }

    private void windowing(double[] data) {
        int length = data.length;
        int overlap = FFT_N/4;
        for (int i = 0; i < overlap; i++) {
            data[i] *= 0.5*(1 + Math.cos(PI + i * PI/overlap));
            data[length - overlap + i] *= 0.5*(1 + Math.cos(i * PI/overlap));
        }
    }

    private short[] doublesToShorts(double[] data) {
        double[] normalizedData = normalize(data);
        short[] result = new short[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (short) (normalizedData[i] * Short.MAX_VALUE);
        }
        return result;
    }

    private double[] normalize(double[] data) {
        int index = findMax(data);
        double max = Math.abs(data[index]);
        for (int i = 0; i < data.length; i++) {
            data[i] /= max;
        }
        return data;
    }

    private int findMax(double[] data) {
        int maxIndex = 0;
        double max = Math.abs(data[maxIndex]);
        for (int i = 0; i < data.length; i++){
            if (Math.abs(data[i]) > max) {
                max = Math.abs(data[i]);
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private short[] mergeSymbols(short[] symbol, int num) {
        short[] result = new short[symbol.length * num + 2*startSignal.length];
        for (int i = 0; i < startSignal.length; i++) {
            result[i] = startSignal[i];
        }
        for (int i = 0; i < symbol.length; i++) {
            int index = 2*startSignal.length;
            for (int j = 0; j < num; j++) {
                result[index + j*symbol.length + i] = symbol[i];
            }
        }
        return result;
    }

    private void hermitian(Complex[] X) {
        int n = X.length;
        for (int i = n - 1; i > n/2; i--) {
            X[i] = X[n - i].conjugate();
        }
    }

    private static short[] creatLFMSignal(int startFre, int endFre, int timeMs, int sampleRate) {
        int signalLength = timeMs*sampleRate/1000;
        short[] signal = new short[signalLength];
        double freInterval = ((double)(endFre - startFre)) / (signalLength - 1);

        for (int i = 0; i < signalLength; i++) {
            double fre = startFre + i*freInterval;
            signal[i] = (short) (Short.MAX_VALUE / 2 * Math.cos(2*Math.PI*fre*i/sampleRate));
        }
        return signal;
    }
}