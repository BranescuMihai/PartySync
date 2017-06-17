package com.branes.partysync.network_communication.nsd_listeners;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.branes.partysync.actions.ServiceDiscoveredActions;
import com.branes.partysync.helper.Constants;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class NsdServiceDiscoveryListener implements NsdManager.DiscoveryListener {

    private static final String TAG = NsdServiceDiscoveryListener.class.getName();

    private String serviceName;
    private ServiceDiscoveredActions serviceDiscoveredActions;
    private NsdManager nsdManager;
    private String groupName;

    public NsdServiceDiscoveryListener(NsdManager nsdManager, String serviceName, String groupName,
                                       ServiceDiscoveredActions serviceDiscoveredActions) {
        this.nsdManager = nsdManager;
        this.serviceName = serviceName;
        this.serviceDiscoveredActions = serviceDiscoveredActions;
        this.groupName = groupName;
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
        } else if (!nsdServiceInfo.getServiceName().contains(groupName)) {
            Log.i(TAG, "Unknown Service Name: " + nsdServiceInfo.getServiceName());
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
        } else if (!nsdServiceInfo.getServiceName().contains(groupName)) {
            Log.i(TAG, "Unknown Service Name: " + nsdServiceInfo.getServiceName());
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
