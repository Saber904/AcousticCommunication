package com.eleven.acoustic.send.generate;

/**
 * Created by Eleven on 2015/11/20.
 */
public abstract class SignalGenerator {
    public abstract short[] doGenerate(int[] data);
}
