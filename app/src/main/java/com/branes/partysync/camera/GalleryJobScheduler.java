package com.branes.partysync.camera;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import com.branes.partysync.helper.IoUtilities;

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
            ObjectObserver.getInstance().updateValue(IoUtilities.readFile(params.getTriggeredContentAuthorities()[0]));
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