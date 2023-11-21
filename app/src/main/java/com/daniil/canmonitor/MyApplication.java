package com.daniil.canmonitor;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication instance;
    private MainActivity mainActivity;

    public MyApplication() {
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}