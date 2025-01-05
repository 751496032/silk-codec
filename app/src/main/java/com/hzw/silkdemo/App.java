package com.hzw.silkdemo;

import android.app.Application;

public class App extends Application {

    public static App context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
