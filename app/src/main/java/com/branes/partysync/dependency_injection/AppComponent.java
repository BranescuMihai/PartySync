package com.branes.partysync.dependency_injection;

import com.branes.partysync.PartySyncApplication;
import com.branes.partysync.activities.main.MainPresenter;
import com.branes.partysync.activities.peers.PeerActivity;
import com.branes.partysync.network_communication.PeerConnectionIncoming;
import com.branes.partysync.network_communication.WifiStateChangedBroadcastReceiver;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(PartySyncApplication application);

    void inject(MainPresenter mainPresenter);

    void inject(PeerActivity peerActivity);

    void inject(WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver);

    void inject(PeerConnectionIncoming peerConnectionIncoming);
}