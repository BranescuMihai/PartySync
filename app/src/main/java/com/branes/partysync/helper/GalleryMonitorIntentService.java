package com.branes.partysync.helper;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
@Deprecated
public class GalleryMonitorIntentService extends Service {

    private GalleryObserver galleryObserver;


    public GalleryMonitorIntentService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (galleryObserver == null) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + Environment.DIRECTORY_DCIM
                    + File.separator + "Camera";
            galleryObserver = new GalleryObserver(path);
            galleryObserver.startWatching();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}