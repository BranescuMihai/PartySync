package com.branes.partysync.activities.main;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.branes.partysync.actions.PeerListChangeActions;
import com.branes.partysync.camera.GalleryJobScheduler;
import com.branes.partysync.camera.ObjectObserver;
import com.branes.partysync.dependency_injection.DependencyInjection;
import com.branes.partysync.helper.IoUtilities;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.NetworkServiceManager;
import com.branes.partysync.network_communication.PeerConnection;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class MainPresenter implements MainContract.Presenter, Observer, PeerListChangeActions {

    private static final String TAG = MainPresenter.class.getName();

    private MainContract.View view;

    @Inject
    NetworkServiceManager networkServiceManager;

    MainPresenter(MainContract.View view) {
        this.view = view;

        DependencyInjection.getAppComponent(view.getContext()).inject(this);

        networkServiceManager.setPeerListChangeActions(this);
        ObjectObserver.getInstance().addObserver(this);
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

    @Override
    public void onPeerListChanged() {
        String displayNumberOfPeers = networkServiceManager.getCommunicationToPeers().size() + "";
        view.setNumberOfPeers(displayNumberOfPeers);
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(TAG, o.toString());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startJobScheduler();
        }

        List<PeerConnection> peers = networkServiceManager.getCommunicationToPeers();

        //concatenate the time of creation to the byte array
        long timeOfCreation = System.currentTimeMillis() / 1000;
        try {
            IoUtilities.createFileFromImage(Utilities.getProfileName().replaceAll("\\s+", ""),
                    String.valueOf(timeOfCreation), (byte[]) o);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] timeInBytes = Longs.toByteArray(timeOfCreation);
        byte[] finalBytes = Bytes.concat(timeInBytes, (byte[]) o);

        for (PeerConnection peerConnection : peers) {
            if (!peerConnection.isConnectionDeactivated()) {
                peerConnection.sendInformation(finalBytes);
            }
        }
    }

    @Override
    public void startServices() {
        startJobScheduler();
        try {
            networkServiceManager.registerNetworkService(view.getGroupName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopServices() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopScheduledJobs(view.getContext());
        }
        networkServiceManager.unregisterNetworkService();
    }

    @Override
    public boolean arePeersConnected() {
        return !networkServiceManager.getCommunicationToPeers().isEmpty();
    }

    private void startJobScheduler() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scheduleJob(view.getContext());
        }
    }
}
