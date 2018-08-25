package com.eleven.acoustic.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Eleven on 2015/12/16.
 */
public class DataWriter {
    private static final String TAG = "WritingData";

    private static int count = 0;

    public static void writeData(final short[] data, final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    String sd_path = Environment.getExternalStorageDirectory().getPath();
                    fos = new FileOutputStream(sd_path + "/" + count + fileName);
                    //fos = MyApplication.getInstance().openFileOutput(fileName, Context.MODE_PRIVATE);
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
                    for (short num : data) {
                        dos.writeShort(num);
                    }
                    dos.flush();
                    count++;
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFound");
                } catch (IOException e) {
                    Log.e(TAG, "IOException");
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
