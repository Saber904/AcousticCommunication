package com.eleven.acoustic.receive.demodulate;

import com.eleven.acoustic.receive.SignalReceiver;

/**
 * Created by Eleven on 2015/12/28.
 */
public abstract class SignalDemodulator {
    protected int[] baseBand; //解调后的基带信号
    public abstract void doDemodulate(double[] data);
    public int[] getBaseBandSignal() {return  baseBand;}
    protected SignalReceiver.OnSignalReceivedListener receivedListener;
    public void setOnReceivedListener (SignalReceiver.OnSignalReceivedListener listener) {
        receivedListener = listener;
    }
}
