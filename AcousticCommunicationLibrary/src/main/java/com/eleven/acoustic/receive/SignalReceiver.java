package com.eleven.acoustic.receive;

import android.media.AudioFormat;
import android.os.Handler;

import com.eleven.acoustic.receive.demodulate.OFDMSignalDemodulator;
import com.eleven.acoustic.receive.demodulate.SignalDemodulator;

/**
 * Created by Eleven on 2015/12/17.
 */
public class SignalReceiver {
    private AudioRecorder mAudioRecorder;
    private SignalDemodulator mSignalDemodulator;
    private final static int DEFAULT_SAMPLE_RATE = 44100;
    //private OnSignalReceivedListener receivedListener;
    private static volatile SignalReceiver mDefault = null;

    private Handler mHandler;

    private boolean isWriteToFile;
    private String mFileName;

    private static final String TAG = "Receive";

    public SignalReceiver(AudioRecorder mAudioRecorder) {
        this(mAudioRecorder, false);
    }

    public SignalReceiver(AudioRecorder mAudioRecorder, boolean isWriteToFile) {
        this.mAudioRecorder = mAudioRecorder;
        this.isWriteToFile = isWriteToFile;

    }

    public static SignalReceiver getDefault() {
        AudioRecordDescriptor audioRecordDescriptor = new AudioRecordDescriptor(DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecorder audioRecorder = new AudioRecorder(audioRecordDescriptor);
        SignalReceiver defaultReceiver = new SignalReceiver(audioRecorder);
        defaultReceiver.setSignalDemodulator(new OFDMSignalDemodulator());
        return defaultReceiver;
    }

    public void setSignalDemodulator(SignalDemodulator signalDemodulator) {
        this.mSignalDemodulator = signalDemodulator;
    }

    public void setWriteToFileAvailable(boolean isWrite) {
        isWriteToFile = isWrite;
    }

    public void setFileName (String fileName) {
        this.mFileName = fileName;
    }

    public void setOnReceivedListener (OnSignalReceivedListener listener) {
        mSignalDemodulator.setOnReceivedListener(listener);
    }

    public void startReceiving() {
        mAudioRecorder.start();

    }

    public void stopReceiving() {
        mAudioRecorder.stop();
        new Thread(new decodeTask()).start();
    }

    public int getSampleRate() {return mAudioRecorder.getRecorderSampleRate();}

    private class decodeTask implements Runnable {
        @Override
        public void run() {
            if (mAudioRecorder.isDataExist()) {
                double[] recvData = mAudioRecorder.getNormalizedData();
                if (mSignalDemodulator != null) {
                    mSignalDemodulator.doDemodulate(recvData);
                }
            }
        }
    }

    public interface OnSignalReceivedListener {
        void onReceived(int[] result);
        void onNotReceived();
    }

}
