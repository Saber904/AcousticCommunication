package com.eleven.acoustic.send;

import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eleven on 2015/11/22.
 */
public class AudioPlayer {

    private ExecutorService executorService;

    private AudioTrackDescriptor mAudioTrackDescriptor;
    private AudioTrack mAudioTrack;
//    private AudioManager mAudioManager;

    private short[] data;

    public void setData(short[] data) {
        this.data = data;
    }

    public AudioPlayer(AudioTrackDescriptor audioTrackDescriptor) {
//        this.mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        this.mAudioTrackDescriptor = audioTrackDescriptor;
        this.mAudioTrack = audioTrackDescriptor.getAudioTrack();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public int getSampleRate() {
        return mAudioTrackDescriptor.getFrequency();
    }

    public void start(final int playTimes, final long silenceMS) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (mAudioTrack != null) {
                    if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
                        mAudioTrack.play();
                } else {
                    mAudioTrack = mAudioTrackDescriptor.getAudioTrack();
                }

                for (int i = 0; i < playTimes; i++) {
                    mAudioTrack.write(data, 0, data.length);
                    try {
                        TimeUnit.MILLISECONDS.sleep(silenceMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mAudioTrack.stop();
            }
        });

    }

    public void start() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (mAudioTrack != null) {
                    mAudioTrack.play();
                }
                mAudioTrack.write(data, 0, data.length);
                mAudioTrack.stop();
            }
        });
    }

    public void stop() {
        executorService.shutdown();
        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioTrack = null;
    }

    public boolean isPlaying() {
        return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
}
