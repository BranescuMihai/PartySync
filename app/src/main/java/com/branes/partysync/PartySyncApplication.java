package com.branes.partysync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.branes.partysync.dependency_injection.AppComponent;
import com.branes.partysync.dependency_injection.AppComponentProvider;
import com.branes.partysync.dependency_injection.AppModule;
import com.branes.partysync.dependency_injection.DaggerAppComponent;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PartySyncApplication extends MultiDexApplication implements AppComponentProvider {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(mContext))
                .build();

        component.inject(this);
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public AppComponent getComponent() {
        return component;
    }
}