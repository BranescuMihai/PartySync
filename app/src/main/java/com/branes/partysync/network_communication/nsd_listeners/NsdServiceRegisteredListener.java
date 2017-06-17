package com.branes.partysync.network_communication.nsd_listeners;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.branes.partysync.actions.ServiceRegisteredListener;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class NsdServiceRegisteredListener implements NsdManager.RegistrationListener {

    private static final String TAG = NsdServiceRegisteredListener.class.getName();

    private ServiceRegisteredListener serviceRegisteredListener;

    public NsdServiceRegisteredListener(ServiceRegisteredListener serviceRegisteredListener) {
        this.serviceRegisteredListener = serviceRegisteredListener;
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
        serviceRegisteredListener.onServiceRegistered(nsdServiceInfo);
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
        Log.e(TAG, "An exception occurred while registering the service: " + errorCode);
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
        serviceRegisteredListener.onServiceUnregistered();
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
        Log.e(TAG, "An exception occurred while un-registering the service: " + errorCode);
    }
}
