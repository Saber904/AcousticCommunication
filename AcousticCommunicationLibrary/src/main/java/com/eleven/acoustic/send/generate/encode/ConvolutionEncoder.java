package com.eleven.acoustic.send.generate.encode;

/**
 * Created by Eleven on 2016/3/30.
 */
public class ConvolutionEncoder implements Encoder{
    private final int n;
    private final int k;
    private final int m;
    public ConvolutionEncoder(int n,int k, int m) {
        this.n = n;
        this.k = k;
        this.m = m;
    }

    @Override
    public int[] encoded(int[] rawData) {
        int[] state = new int[m-1];
        int[] output = new int[2*rawData.length+2*(m-1)];
        for (int i=0; i < rawData.length; i++) {
            output[2*i]=(rawData[i]^state[0])^state[1];
            output[2*i+1]=rawData[i]^state[1];
            state[1]=state[0];
            state[0]=rawData[i];
        }
        for (int i=0; i < m-1; i++){
            output[2*rawData.length+2*i]=(0^state[0])^state[1];
            output[2*rawData.length+2*i+1]=0^state[1];
            state[1]=state[0];
            state[0]=0;
        }
        return output;
    }
}
