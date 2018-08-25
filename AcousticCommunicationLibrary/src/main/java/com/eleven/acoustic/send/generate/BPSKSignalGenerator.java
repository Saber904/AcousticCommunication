package com.eleven.acoustic.send.generate;

/**
 * Created by Eleven on 2015/12/11.
 */
public class BPSKSignalGenerator extends SignalGenerator {
    private int mCodeIntervalMS; //符号间隔,单位ms
    private int mCarrierFrequence; //载波频率
    private int mSampleRate; //采样频率
    private int[] mRowData;

    public BPSKSignalGenerator(int sampleRate) {
        this.mSampleRate = sampleRate;
    }

    public BPSKSignalGenerator(int codeIntervalMS, int carrierFrequence, int sampleRate) {
        this.mCodeIntervalMS = codeIntervalMS;
        this.mCarrierFrequence = carrierFrequence;
        this.mSampleRate = sampleRate;
    }

    public void setCarrierFrequence(int frequence) {
        this.mCarrierFrequence = frequence;
    }

    public void setCodeIntervalMS (int codeIntervalMS) {
        this.mCodeIntervalMS = codeIntervalMS;
    }

    @Override
    public short[] doGenerate(int[] data) {
        mRowData = data;

        if (mRowData == null) {
            return null;
        }

        int codeLength = mRowData.length * 4;
        byte[] binaryCodes = new byte[codeLength];

        for (int i = 0; i < mRowData.length; i++) {
            for (int j = 0; j < 4; j++) {
                binaryCodes[4 * i + j] = (byte) (mRowData[i] >> (3 - j) & 1);
            }
        }

        int codeTime = mCodeIntervalMS * mSampleRate / 1000;

        short[] signal = new short[codeLength * codeTime];

        for (int i = 0; i < codeLength; i++) {
            if (binaryCodes[i] == 0) {
                for (int j = 0; j < codeTime; j++) {
                    signal[i * codeTime + j] = (short)
                            (- Short.MAX_VALUE / 2 * Math.cos(2 * Math.PI * mCarrierFrequence * j / mSampleRate));
                }
            } else {
                for (int j = 0; j < codeTime; j++) {
                    signal[i * codeTime + j] = (short)
                            (Short.MAX_VALUE / 2 * Math.cos(2 * Math.PI * mCarrierFrequence * j / mSampleRate));
                }
            }
        }

        return signal;
    }
}
