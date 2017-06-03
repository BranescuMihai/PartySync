package com.branes.partysync.network_communication;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.branes.partysync.actions.ServiceDiscoveredActions;
import com.branes.partysync.helper.Constants;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
class NsdServiceDiscoveryListener implements NsdManager.DiscoveryListener {

    private static final String TAG = NsdServiceResolvedListener.class.getName();

    private String serviceName;
    private ServiceDiscoveredActions serviceDiscoveredActions;
    private NsdManager nsdManager;

    NsdServiceDiscoveryListener(NsdManager nsdManager, String serviceName, ServiceDiscoveredActions serviceDiscoveredActions) {
        this.nsdManager = nsdManager;
        this.serviceName = serviceName;
        this.serviceDiscoveredActions = serviceDiscoveredActions;
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.i(TAG, "Service discovery started: " + serviceType);
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        Log.i(TAG, "Service found: " + nsdServiceInfo);
        if (!nsdServiceInfo.getServiceType().equals(Constants.SERVICE_TYPE)) {
            Log.i(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
        } else if (nsdServiceInfo.getServiceName().equals(serviceName)) {
            Log.i(TAG, "The service running on the same machine has been discovered: " + serviceName);
        } else if (nsdServiceInfo.getServiceName().contains(Constants.SERVICE_NAME)) {
            serviceDiscoveredActions.onServiceFound(nsdServiceInfo);
        }
    }

    @Override
    public void onServiceLost(final NsdServiceInfo nsdServiceInfo) {
        Log.i(TAG, "Service lost: " + nsdServiceInfo);
        if (!nsdServiceInfo.getServiceType().equals(Constants.SERVICE_TYPE)) {
            Log.i(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
        } else if (nsdServiceInfo.getServiceName().equals(serviceName)) {
            Log.i(TAG, "The service running on the same machine has been discovered: " + serviceName);
        } else {
            serviceDiscoveredActions.onServiceLost(nsdServiceInfo);
        }
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG, "Service discovery stopped: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Service discovery start failed: Error code:" + errorCode);
        nsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Service discovery stop failed: Error code:" + errorCode);
        nsdManager.stopServiceDiscovery(this);
    }
}
