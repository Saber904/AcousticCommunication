package com.eleven.acoustic.send.generate;

import android.util.Log;

import com.eleven.acoustic.send.generate.encode.Encoder;
import com.eleven.acoustic.send.generate.encode.GrayEncoder;

/**
 * Created by Eleven on 2015/12/23.
 */
public class DPSKSignalGenerator extends SignalGenerator {
    private final int mCodeIntervalMS = 10; //符号间隔,单位ms
    private final int mCarrierFrequence = 5000; //载波频率
    private final int mSampleRate = 44100; //采样频率
    private int[] mRawData;
    private Encoder encoder;

    public DPSKSignalGenerator() {
        encoder = new GrayEncoder();
    }


//    public void setCarrierFrequence(int frequence) {
//        this.mCarrierFrequence = frequence;
//    }
//
//    public void setCodeIntervalMS (int codeIntervalMS) {
//        this.mCodeIntervalMS = codeIntervalMS;
//    }

    @Override
    public short[] doGenerate(int[] data) {
        Log.i("SignalGenerate", "DPSK signal");
        mRawData = data;

        if (mRawData == null) {
            return null;
        }

        int[] abs_binaryCodes = encoder.encoded(mRawData);

        int[] relative_binaryCodes = new int[abs_binaryCodes.length + 1];
        relative_binaryCodes[0] = 1;
        for (int i = 1; i < relative_binaryCodes.length; i++){
            relative_binaryCodes[i] = relative_binaryCodes[i - 1] ^ abs_binaryCodes[i - 1];
        }

        int codeTime = mCodeIntervalMS * mSampleRate / 1000;
        short[] signal = new short[relative_binaryCodes.length * codeTime];

        for (int i = 0; i < relative_binaryCodes.length; i++) {
            if (relative_binaryCodes[i] == 0) {
                for (int j = 0; j < codeTime; j++) {
                    signal[i * codeTime + j] = (short)
                            (-Short.MAX_VALUE / 2 * Math.cos(2 * Math.PI * mCarrierFrequence * j / mSampleRate));
                }
            } else {
                for (int j = 0; j < codeTime; j++) {
                    signal[i * codeTime + j] = (short)
                            (Short.MAX_VALUE / 2 * Math.cos(2 * Math.PI * mCarrierFrequence * j / mSampleRate));
                }
            }
        }
        short[] startSignal = creatLFMSignal(4000, 6000, 20);

        short[] interval = new short[882];

        short[] endSignal = creatLFMSignal(6000, 4000, 20);

//        short[] endSignal = new short[(int) Math.floor(mSampleRate * 0.01)]; //10ms终止信号
//        for (int i = 0; i < endSignal.length; i++) {
//            endSignal[i] = (short)
//                    (Short.MAX_VALUE / 2 * Math.cos(2 * Math.PI * 8000 * i / mSampleRate));
//        }
        return mergeSignals(startSignal, signal, endSignal, interval);
    }

    private short[] mergeSignals(short[] start, short[] signal, short[] end, short[] interval) {
        short[] part1 = mergeTwoArrays(start, interval);
        short[] part2 = mergeTwoArrays(part1, signal);
        short[] part3 = mergeTwoArrays(interval, end);
        return mergeTwoArrays(part2, part3);
    }

    private short[] mergeTwoArrays(short[] array1, short[] array2) {
        short[] array3 = new short[array1.length + array2.length];
        for (int i = 0; i < array3.length; i++){
            if (i < array1.length) {
                array3[i] = array1[i];
            } else {
                array3[i] = array2[i - array1.length];
            }
        }
        return array3;
    }

    private short[] creatLFMSignal(int startFre, int endFre, int timeMs) {
        int signalLength = timeMs*mSampleRate/1000;
        short[] signal = new short[signalLength];
        double freInterval = ((double)(endFre - startFre)) / (signalLength - 1);

        for (int i = 0; i < signalLength; i++) {
            double fre = startFre + i*freInterval;
            signal[i] = (short) (Short.MAX_VALUE / 2 * Math.cos(2*Math.PI*fre*i/mSampleRate));
        }
        return signal;
    }
}
