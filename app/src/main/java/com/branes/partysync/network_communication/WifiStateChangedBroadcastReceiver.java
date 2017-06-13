package com.branes.partysync.network_communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.branes.partysync.dependency_injection.DependencyInjection;

import javax.inject.Inject;


/**
 * Copyright © 2017 Deutsche Bank. All rights reserved.
 */
public class WifiStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Inject
    NetworkServiceManager networkServiceManager;

    public WifiStateChangedBroadcastReceiver(Context context) {
        DependencyInjection.getAppComponent(context).inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo ni = getNetworkInfo(intent);

        if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
            if (ni.getState().toString().equals("CONNECTED")) {
                try {
                    networkServiceManager.registerNetworkService(networkServiceManager.personalUsername);
                    networkServiceManager.startNetworkServiceDiscovery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                networkServiceManager.unregisterNetworkService();
                networkServiceManager.stopNetworkServiceDiscovery();
            }
        }
    }

    private NetworkInfo getNetworkInfo(Intent intent) {
        Bundle extras = intent.getExtras();
        return (NetworkInfo) (extras.get("networkInfo"));
    }
}