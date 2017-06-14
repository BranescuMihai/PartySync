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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class NetworkServiceManager implements AuthenticationFailureActions,
        ServiceResolvedActions, ServiceDiscoveredActions, ServiceRegisteredListener {

    private static final String TAG = NetworkServiceManager.class.getName();

    String groupName;

    private String serviceName;
    private PeerConnectionIncoming peerConnectionIncoming;
    private List<PeerConnection> communicationToPeers;
    private List<PeerConnection> communicationFromClients;

    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.RegistrationListener registrationListener;

    private PeerListChangeActions peerListChangeActions;

    private HashMap<String, Integer> failedToResolveServices;

    public NetworkServiceManager(Context context) {

        this.communicationToPeers = new ArrayList<>();
        this.communicationFromClients = new ArrayList<>();

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
        if (nsdServiceInfo.getHost() != null) {
            PeerConnection client = getChatClientIfExists(retrieveHost(nsdServiceInfo));

            if (client != null) {
                communicationToPeers.remove(client);
                peerListChangeActions.onPeerListChanged();
            }
        }
    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {
        String host = retrieveHost(serviceInfo);

        int port = serviceInfo.getPort();
        String username = serviceInfo.getServiceName().split("-")[2];

        if (getChatClientIfExists(host) == null) {
            final PeerConnection peerConnection = new PeerConnection(this, host, port, username);
            Handler hand = new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection.getSocket() != null) {
                        communicationToPeers.add(peerConnection);
                        peerListChangeActions.onPeerListChanged();
                    }
                }
            }, 600);
            Log.i(TAG, "A service has been discovered on " + host + ":" + port);
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
                    nsdManager.resolveService(serviceInfo, new NsdServiceResolvedListener(serviceName, NetworkServiceManager.this));
                }
            }, 1000 * failedToResolveServices.get(foreignServiceName));
        }
    }

    @Override
    public void onAuthenticationFailed(Socket socket) {
        for (PeerConnection peerConnection : communicationToPeers) {
            if (peerConnection.getSocket().equals(socket)) {
                communicationToPeers.remove(peerConnection);
                peerListChangeActions.onPeerListChanged();
                break;
            }
        }
    }

    public void registerNetworkService(String groupName) throws Exception {

        if (peerConnectionIncoming != null && peerConnectionIncoming.isAlive()) {
            return;
        }

        this.groupName = groupName;

        peerConnectionIncoming = new PeerConnectionIncoming(this);
        ServerSocket serverSocket = peerConnectionIncoming.getServerSocket();
        if (serverSocket == null) {
            throw new Exception("Could not get server socket");
        }

        Log.v(TAG, "Register Network Service on Port " + serverSocket.getLocalPort());
        peerConnectionIncoming.start();

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();

        nsdServiceInfo.setServiceName(Constants.SERVICE_NAME + Utilities.generateIdentifier(Constants.IDENTIFIER_LENGTH) +
                groupName);
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
            for (PeerConnection communicationFromClient : communicationFromClients) {
                communicationFromClient.stopThreads();
            }
            communicationFromClients.clear();
            peerConnectionIncoming.stopThread();
        }
    }

    public void startNetworkServiceDiscovery() {
        Log.v(TAG, "Start Network Service Discovery");
        discoveryListener = new NsdServiceDiscoveryListener(nsdManager, serviceName, groupName, this);
        nsdManager.discoverServices(Constants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopNetworkServiceDiscovery() {
        if (discoveryListener != null) {
            Log.v(TAG, "Stop Network Service Discovery");
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener = null;

            for (PeerConnection communicationToServer : communicationToPeers) {
                communicationToServer.stopThreads();
            }
            communicationToPeers.clear();
            peerListChangeActions.onPeerListChanged();
        }
    }

    public List<PeerConnection> getCommunicationToPeers() {
        return communicationToPeers;
    }

    List<PeerConnection> getCommunicationFromClients() {
        return communicationFromClients;
    }

    void setCommunicationFromClients(List<PeerConnection> communicationFromClients) {
        this.communicationFromClients = communicationFromClients;
    }

    private PeerConnection getChatClientIfExists(String host) {
        for (PeerConnection client : communicationToPeers) {
            if (client.getHost().equals(host)) {
                return client;
            }
        }
        return null;
    }

    private String retrieveHost(NsdServiceInfo serviceInfo) {
        String host = serviceInfo.getHost().toString();
        if (host.startsWith("/")) {
            return host.substring(1);
        }
        return host;
    }
}