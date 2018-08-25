package com.eleven.acoustic.receive;

import android.media.AudioRecord;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eleven on 2015/12/17.
 */
public class AudioRecorder {
    private AudioRecordDescriptor mAudioRecordDescriptor;

    private AudioRecord mAudioRecord;

    private ArrayList<short[]> mData = new ArrayList<>();

    private ExecutorService mExecutorService;
    Thread recThread;
    private volatile boolean isRecording;

    public AudioRecorder(AudioRecordDescriptor mAudioRecordDescriptor) {
        this.mAudioRecordDescriptor = mAudioRecordDescriptor;
        mAudioRecord = mAudioRecordDescriptor.getAudioRecord();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public void start() {
        mData.clear();
        isRecording = true;
        mExecutorService.execute(new recTask());
    }

    public void stop() {
        isRecording = false;
        try {
            mExecutorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isDataExist() { return mData.size() != 0;}

    public synchronized short[] getRawData() {
        if (mData.size() == 0) {
            return null;
        }
        int dataLength = mData.get(0).length * mData.size();
        short[] data = new short[dataLength];

        int count = 0;
        for (int i = 0; i < mData.size(); i++) {
            short[] temp = mData.get(i);
            for (int j = 0; j < temp.length; j++) {
                data[count + j] = temp[j];
            }
            count += temp.length;
        }

//        Iterator<short[]> it = mData.listIterator();
//        int count = 0;
//        while (it.hasNext()) {
//            short[] temp = it.next();
//            for (int i = 0; i < temp.length; i++){
//                data[count + i] = temp[i];
//            }
//            count += temp.length;
//        }
        mData.clear();
        return data;
    }

    public int getRecorderSampleRate() {
        return mAudioRecordDescriptor.getFrequency();
    }

    public double[] getNormalizedData() {
        double[] data = null;
        synchronized (mData) {
            if (mData.size() == 0) {
                return null;
            }
            int dataLength = mData.get(0).length * mData.size();
            data = new double[dataLength];

            int count = 0;
            for (int i = 0; i < mData.size(); i++) {
                short[] temp = mData.get(i);
                for (int j = 0; j < temp.length; j++) {
                    data[count + j] = ((double) temp[j]) / Short.MAX_VALUE;
                }
                count += temp.length;
            }
//        Iterator<short[]> it = mData.listIterator();
//        int count = 0;
//        while (it.hasNext()) {
//            short[] temp = it.next();
//            for (int i = 0; i < temp.length; i++){
//                data[count + i] = ((double) temp[i]) / Short.MAX_VALUE;
//            }
//            count += temp.length;
//        }
            mData.clear();
        }
        return data;
    }

    private class recTask implements Runnable {
        @Override
        public void run() {
            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException e) {
                Log.e("RECORD", "NOTINTIALIZED");
            }

            while (isRecording) {
                int bufferSize = mAudioRecordDescriptor.getBufferSizeInBytes();
                short[] buffer = new short[bufferSize];
                int readLength = mAudioRecord.read(buffer, 0, bufferSize);

                if (readLength > 0) {
                    synchronized (mData) {
                        mData.add(buffer);
                    }
                }
            }
            mAudioRecord.stop();
        }
    }
}
