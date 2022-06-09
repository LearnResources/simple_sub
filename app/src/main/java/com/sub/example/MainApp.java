package com.sub.example;

import android.app.Application;

import com.sub.example.sub.Sub;

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Sub.init(this);
    }
}
