package com.branes.partysync.activities.main;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.MediaStore;

import com.branes.partysync.camera.GalleryJobScheduler;

/**
 * Copyright (c) 2017 Mihai Branescu
 */

class MainPresenter implements MainContract.Presenter {

    private MainContract.View view;

    MainPresenter(MainContract.View view) {
        this.view = view;
    }

    @Override
    public void startJobScheduler() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scheduleJob(view.getContext());
        }
    }

    @Override
    public void stopJobScheduler() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopScheduledJobs(view.getContext());
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void scheduleJob(Context context) {
        JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(
                0,
                new ComponentName(context, GalleryJobScheduler.class));
        builder.addTriggerContentUri(
                new JobInfo.TriggerContentUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        js.schedule(builder.build());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopScheduledJobs(Context context) {
        JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancelAll();
    }
}
