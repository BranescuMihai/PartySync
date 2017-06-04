package com.branes.partysync.dependency_injection;

import com.branes.partysync.PartySyncApplication;
import com.branes.partysync.activities.main.MainActivity;
import com.branes.partysync.activities.peers.PeerActivity;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(PartySyncApplication application);

    void inject(MainActivity mainActivity);

    void inject(PeerActivity peerActivity);

}