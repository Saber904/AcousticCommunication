package com.eleven.acoustic.send.generate.encode;

/**
 * Created by Eleven on 2016/3/22.
 */
public class GrayEncoder implements Encoder{
    @Override
    public int[] encoded(int[] rawData) {
        int[] grayCode = new int[rawData.length*4];
        int[] binaryCode = new int[rawData.length*4];
        for (int i = 0; i < rawData.length; i++) {
            for (int j = 0; j < 4; j++) {
                binaryCode[4 * i + j] =  (rawData[i] >> (3 - j) & 1);
            }
            for (int j = 0; j < 4; j++) {
                if (j == 0)
                    grayCode[4*i + j] = binaryCode[4*i +j];
                else
                    grayCode[4*i + j] = binaryCode[4*i + j]^binaryCode[4*i + j - 1];
            }
        }
        return grayCode;
    }
}
