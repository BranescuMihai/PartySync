package com.branes.partysync;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PartySyncApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}