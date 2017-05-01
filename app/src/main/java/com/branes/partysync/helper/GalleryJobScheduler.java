package com.branes.partysync.helper;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

/**
 * Copyright (c) 2017 Mihai Branescu
 */

@TargetApi(Build.VERSION_CODES.N)
public class GalleryJobScheduler extends JobService {

    private static final String TAG = GalleryJobScheduler.class.getName();

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            ObjectObserver.getInstance().updateValue(IOUtil.readFile(params.getTriggeredContentAuthorities()[0]));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't retrieve picture");
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}