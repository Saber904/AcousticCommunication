package com.eleven.acoustic.receive;

import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Eleven on 2015/12/16.
 */
public class AudioRecordDescriptor {
    private final int frequency;
    private final int channelConfiguration;
    private final int audioEncoding;

    private int recBufSize;

    public AudioRecordDescriptor(int frequency, int channelConfiguration, int audioEncoding) {
        this.frequency = frequency;
        this.channelConfiguration = channelConfiguration;
        this.audioEncoding = audioEncoding;
    }

    public int getBufferSizeInBytes() {
        return recBufSize;
    }

    public int getFrequency() {return frequency;}

    public AudioRecord getAudioRecord() {
        recBufSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);
        return new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, 2*recBufSize);
    }
}
