package com.eleven.acoustic.send;

import android.media.AudioTrack;

/**
 * Created by Eleven on 2015/11/22.
 */
public class AudioTrackDescriptor {
    private final int streamType;
    private final int frequency;
    private final int channelConfig;
    private final int audioFormat;
    private final int mode;


    public int getAudioFormat() {
        return audioFormat;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getMode() {
        return mode;
    }

    public int getStreamType() {
        return streamType;
    }

    /**
     * see {@link AudioTrack#AudioTrack(int, int, int, int, int, int)}
     * @param streamType
     * @param frequency
     * @param channelConfig
     * @param audioFormat
     * @param mode
     */
    public AudioTrackDescriptor(int streamType, int frequency, int channelConfig, int audioFormat, int mode) {
        this.streamType = streamType;
        this.frequency = frequency;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.mode = mode;
    }

    public AudioTrack getAudioTrack() {
        int minSize = AudioTrack.getMinBufferSize(frequency, channelConfig, audioFormat);
        return new AudioTrack(streamType, frequency, channelConfig, audioFormat, minSize, mode);
    }
}
