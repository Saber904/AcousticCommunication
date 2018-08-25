package com.eleven.acoustic.receive.demodulate.decode;

/**
 * Created by Eleven on 2016/4/15.
 */
public class ConvolutionDecoder implements Decoder {
    @Override
    public int[] decode(int[] binaryCodes) {
        int length = binaryCodes.length/2;
        int[] d_pre = new int[4];
        int[] d = new int[4];
        int[][] output_pre = new int[4][length];
        int[][] output = new int[4][length];
        for (int i = 0; i < length; i++) {
            int QPSK = binaryCodes[2*i]*2+binaryCodes[2*i+1];
            if (i == 0) {
                d[0] = compare(QPSK, 0);
                d[1] = compare(QPSK, 3);
                output[0][0] = 0;
                output[1][0] = 0;
                output[2][0] = 1;
                output[3][0] = 1;
            } else if (i == 1) {
                d[0] = d_pre[0] + compare(QPSK, 0);
                d[1] = d_pre[0] + compare(QPSK, 3);
                d[2] = d_pre[1] + compare(QPSK, 2);
                d[3] = d_pre[1] + compare(QPSK, 1);
                output[0][1] = 0;
                output[1][1] = 1;
                output[2][1] = 0;
                output[3][1] = 1;
            } else {
                if (compare(QPSK, 0) < compare(QPSK, 3)){
                    d[0] = d_pre[0] + compare(QPSK, 0);
                    output[0] = output_pre[0];
                    output[0][i] = 0;
                } else {
                    d[0] = d_pre[2] + compare(QPSK, 3);
                    output[0] = output_pre[2];
                    output[0][i] = 0;
                }
                if (compare(QPSK, 3) < compare(QPSK, 0)){
                    d[1] = d_pre[0] + compare(QPSK, 3);
                    output[1] = output_pre[0];
                    output[1][i] = 1;
                } else {
                    d[1] = d_pre[2] + compare(QPSK, 0);
                    output[1] = output_pre[2];
                    output[1][i] = 1;
                }
                if (compare(QPSK, 1) < compare(QPSK, 2)){
                    d[2] = d_pre[3] + compare(QPSK, 1);
                    output[2] = output_pre[3];
                    output[2][i] = 0;
                } else {
                    d[2] = d_pre[1] + compare(QPSK, 2);
                    output[2] = output_pre[1];
                    output[2][i] = 0;
                }
                if (compare(QPSK, 1) < compare(QPSK, 2)){
                    d[3] = d_pre[1] + compare(QPSK, 1);
                    output[3] = output_pre[1];
                    output[3][i] = 1;
                } else {
                    d[3] = d_pre[3] + compare(QPSK, 2);
                    output[3] = output_pre[3];
                    output[3][i] = 1;
                }
            }
            output_pre = output.clone();
            d_pre = d.clone();
        }
        return output[0];
    }

    private int compare(int lhs, int rhs) {
        int a1 = lhs/2;
        int a2 = rhs/2;
        int b1 = lhs%2;
        int b2 = rhs%2;
        int result = 0;
        if (a1 != a2)
            result += 1;
        if (b1 != b2)
            result += 1;
        return result;
    }
}
