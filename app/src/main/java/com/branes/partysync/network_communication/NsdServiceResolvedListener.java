package com.branes.partysync.network_communication;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.branes.partysync.actions.ServiceResolvedActions;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
class NsdServiceResolvedListener implements NsdManager.ResolveListener {

    private static final String TAG = NsdServiceResolvedListener.class.getName();

    private String serviceName;
    private ServiceResolvedActions serviceResolvedActions;

    NsdServiceResolvedListener(String serviceName, ServiceResolvedActions serviceResolvedActions) {
        this.serviceName = serviceName;
        this.serviceResolvedActions = serviceResolvedActions;
    }

    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "Resolve failed: " + errorCode);
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        Log.i(TAG, "Resolve succeeded: " + serviceInfo);

        if (serviceInfo.getServiceName().equals(serviceName)) {
            Log.i(TAG, "The service running on the same machine has been discovered.");
            return;
        }

        serviceResolvedActions.onServiceResolved(serviceInfo, serviceName);
    }
}
