package com.branes.partysync.network_communication;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;

import com.branes.partysync.actions.AuthenticationFailureActions;
import com.branes.partysync.actions.PeerListChangeActions;
import com.branes.partysync.actions.ServiceDiscoveredActions;
import com.branes.partysync.actions.ServiceRegisteredListener;
import com.branes.partysync.actions.ServiceResolvedActions;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.model.Connections;
import com.branes.partysync.network_communication.nsd_listeners.NsdServiceDiscoveryListener;
import com.branes.partysync.network_communication.nsd_listeners.NsdServiceRegisteredListener;
import com.branes.partysync.network_communication.nsd_listeners.NsdServiceResolvedListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class NetworkServiceManager implements AuthenticationFailureActions,
        ServiceResolvedActions, ServiceDiscoveredActions, ServiceRegisteredListener {

    private static final String TAG = NetworkServiceManager.class.getName();

    private String serviceName;
    private PeerConnectionIncoming peerConnectionIncoming;

    private Connections connections;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.RegistrationListener registrationListener;

    private PeerListChangeActions peerListChangeActions;

    private HashMap<String, Integer> failedToResolveServices;

    public NetworkServiceManager(Context context) {
        connections = new Connections();
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        failedToResolveServices = new HashMap<>();
    }

    public void setPeerListChangeActions(PeerListChangeActions peerListChangeActions) {
        this.peerListChangeActions = peerListChangeActions;
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
        serviceName = nsdServiceInfo.getServiceName();
        startNetworkServiceDiscovery();
    }

    @Override
    public void onServiceUnregistered() {
        stopNetworkServiceDiscovery();
    }

    @Override
    public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
        nsdManager.resolveService(nsdServiceInfo, new NsdServiceResolvedListener(serviceName, NetworkServiceManager.this));
    }

    @Override
    public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
        if (nsdServiceInfo.getServiceName() != null
                && connections.removeConnection(nsdServiceInfo.getServiceName())) {
            peerListChangeActions.onPeerListChanged();
        }
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        String host = retrieveHost(serviceInfo);
        int port = serviceInfo.getPort();

        if (connections.addConnection(this, host, port)) {
            Log.i(TAG, "A service has been discovered on " + host + ":" + port);
            peerListChangeActions.onPeerListChanged();
        }
    }

    @Override
    public void onServiceFailed(final NsdServiceInfo serviceInfo) {
        final String foreignServiceName = serviceInfo.getServiceName();
        if (foreignServiceName.equals(serviceName)) {
            return;
        }

        if (!failedToResolveServices.containsKey(foreignServiceName)) {
            failedToResolveServices.put(foreignServiceName, 1);
        } else if (failedToResolveServices.get(foreignServiceName) < 3) {
            failedToResolveServices.put(foreignServiceName, failedToResolveServices.get(foreignServiceName) + 1);
        } else {
            //we allow only three retries
            failedToResolveServices.remove(foreignServiceName);
        }

        //if it hasn't been removed
        if (failedToResolveServices.containsKey(foreignServiceName)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("Error", 5000 * failedToResolveServices.get(foreignServiceName) + " " + foreignServiceName);
                    nsdManager.resolveService(serviceInfo, new NsdServiceResolvedListener(serviceName, NetworkServiceManager.this));
                }
            }, 5000 * failedToResolveServices.get(foreignServiceName));
        }
    }

    @Override
    public void onAuthenticationFailed(Socket socket) {
        if (connections.removeConnection(socket)) {
            peerListChangeActions.onPeerListChanged();
        }
    }

    public void registerNetworkService(String groupName) throws Exception {

        if (peerConnectionIncoming != null && peerConnectionIncoming.isAlive()) {
            return;
        }

        Utilities.setGroupName(groupName);

        Utilities.setOwnServiceName(Constants.SERVICE_NAME + Utilities.generateIdentifier(Constants.IDENTIFIER_LENGTH) +
                groupName);

        peerConnectionIncoming = new PeerConnectionIncoming();
        ServerSocket serverSocket = peerConnectionIncoming.getServerSocket();
        if (serverSocket == null) {
            throw new Exception("Could not get server socket");
        }

        Log.v(TAG, "Register Network Service on Port " + serverSocket.getLocalPort());
        peerConnectionIncoming.start();

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();

        nsdServiceInfo.setServiceName(Utilities.getOwnServiceName());
        nsdServiceInfo.setServiceType(Constants.SERVICE_TYPE);
        nsdServiceInfo.setHost(serverSocket.getInetAddress());
        nsdServiceInfo.setPort(serverSocket.getLocalPort());

        registrationListener = new NsdServiceRegisteredListener(this);
        nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void unregisterNetworkService() {
        if (peerConnectionIncoming != null && peerConnectionIncoming.isAlive()) {
            Log.v(TAG, "Unregister Network Service");
            nsdManager.unregisterService(registrationListener);

            try {
                peerConnectionIncoming.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            peerConnectionIncoming.stopThread();
        }
    }

    public Connections getConnections() {
        return connections;
    }

    private void startNetworkServiceDiscovery() {
        Log.v(TAG, "Start Network Service Discovery");
        discoveryListener = new NsdServiceDiscoveryListener(nsdManager, serviceName, Utilities.getGroupName(), this);
        nsdManager.discoverServices(Constants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void stopNetworkServiceDiscovery() {
        if (discoveryListener != null) {
            Log.v(TAG, "Stop Network Service Discovery");
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener = null;

            connections.clearConnections();
            peerListChangeActions.onPeerListChanged();
        }
    }

    private String retrieveHost(NsdServiceInfo serviceInfo) {
        String host = serviceInfo.getHost().toString();
        if (host.startsWith("/")) {
            return host.substring(1);
        }
        return host;
    }
}