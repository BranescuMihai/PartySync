package com.branes.partysync.helper;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.branes.partysync.model.NetworkService;
import com.branes.partysync.network_communication.ChatClient;

import java.util.ArrayList;

/**
 * Copyright (c) 2017 Mihai Branescu
 */

public class NsdServiceResolvedListener implements NsdManager.ResolveListener {
    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {

    }

//    private static final String TAG = NsdServiceResolvedListener.class.getName();
//
//    public NsdServiceResolvedListener(String serviceName, ) {
//
//    }
//
//    @Override
//    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
//        Log.e(TAG, "Resolve failed: " + errorCode);
//    }
//
//    @Override
//    public void onServiceResolved(NsdServiceInfo serviceInfo) {
//        Log.i(TAG, "Resolve succeeded: " + serviceInfo);
//
//        if (serviceInfo.getServiceName().equals(serviceName)) {
//            Log.i(TAG, "The service running on the same machine has been discovered.");
//            return;
//        }
//
//        String host = serviceInfo.getHost().toString();
//        if (host.startsWith("/")) {
//            host = host.substring(1);
//        }
//
//        int port = serviceInfo.getPort();
//        ArrayList<NetworkService> discoveredServices = mainActivity.getDiscoveredServices();
//        NetworkService networkService = new NetworkService(serviceInfo.getServiceName(), host, port, Constants.CONVERSATION_TO_SERVER);
//        if (!discoveredServices.contains(networkService)) {
//            ChatClient chatClient = new ChatClient(null, host, port);
//            if (chatClient.getSocket() != null) {
//                communicationToServers.add(chatClient);
//                discoveredServices.add(networkService);
//                mainActivity.setDiscoveredServices(discoveredServices);
//            }
//        }
//
//        Log.i(TAG, "A service has been discovered on " + host + ":" + port);
//    }
}
