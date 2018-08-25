package com.eleven.acoustic.send;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.eleven.acoustic.send.generate.OFDMSignalGenerator;
import com.eleven.acoustic.send.generate.SignalGenerator;
import com.eleven.acoustic.util.DataWriter;

/**
 * Created by Eleven on 2015/11/20.
 */
public class SignalSender
{
    private AudioPlayer mAudioPlayer;
    private SignalGenerator mSignalGenerator;
    private boolean isWriteData;
    private String mFileName;
    private static volatile SignalSender mDefault;
    private final static int DEFAULT_SAMPLE_RATE = 44100;

    public SignalSender(AudioPlayer audioPlayer) {
        this(audioPlayer, false);
    }

    public SignalSender( AudioPlayer mAudioPlayer, boolean isWriteData) {
        this.mAudioPlayer = mAudioPlayer;
        this.isWriteData = isWriteData;
    }

    public static SignalSender getDefault() {

        AudioTrackDescriptor audioTrackDescriptor = new AudioTrackDescriptor(AudioManager.STREAM_MUSIC, DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, AudioTrack.MODE_STREAM);
        AudioPlayer audioPlayer = new AudioPlayer(audioTrackDescriptor);
        SignalSender defaultSender = new SignalSender(audioPlayer);
        defaultSender.setSignalGenerator(new OFDMSignalGenerator());
        return defaultSender;
    }

    public void setWriteToFileAvailable(boolean isWrite) {
        isWriteData = isWrite;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public SignalGenerator getSignalGenerator() {
        return mSignalGenerator;
    }

    public void setSignalGenerator(SignalGenerator signalGenerator) {
        this.mSignalGenerator = signalGenerator;
    }

    private void generateSignal(int[] rowData){
        if (isWriteData) {
            short[] data = mSignalGenerator.doGenerate(rowData);
            DataWriter.writeData(data, mFileName);
            mAudioPlayer.setData(data);
        } else {
            mAudioPlayer.setData(mSignalGenerator.doGenerate(rowData));
        }
    }

    public void sendSignal(int[] rawData, int playTimes, long silenceMS) {
        if(mSignalGenerator != null) {
            generateSignal(rawData);
        }
        mAudioPlayer.start(playTimes, silenceMS);
    }

    public void sendSignal(int[] rowData) {
        if(mSignalGenerator != null) {
            generateSignal(rowData);
        }
        mAudioPlayer.start(1, 50);
    }

    public void stop() {mAudioPlayer.stop();}

}
