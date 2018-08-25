package com.eleven.acoustic.demo;

import android.app.Application;

/**
 * Created by Eleven on 2015/12/16.
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance(){
        return instance;
    }

    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
