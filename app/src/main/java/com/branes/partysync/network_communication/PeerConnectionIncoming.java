package com.branes.partysync.network_communication;

import android.util.Log;

import com.branes.partysync.helper.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
class PeerConnectionIncoming extends Thread {

    private static final String TAG = PeerConnectionIncoming.class.getName();

    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private ServerSocket serverSocket = null;

    PeerConnectionIncoming(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException ioException) {
            Log.e(TAG, "An error has occurred during server run: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(TAG, "Waiting for a connection...");
                Socket socket = serverSocket.accept();
                Log.i(TAG, "Received a connection request from: " + socket.getInetAddress() + ":" + socket.getLocalPort());
                List<PeerConnection> communicationFromClients = networkServiceDiscoveryOperations.getCommunicationFromClients();
                communicationFromClients.add(new PeerConnection(networkServiceDiscoveryOperations, socket));
                networkServiceDiscoveryOperations.setCommunicationFromClients(communicationFromClients);
            }
        } catch (IOException ioException) {
            Log.e(TAG, "An error has occurred during server run: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    void stopThread() {
        interrupt();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ioException) {
            Log.e(TAG, "An error has occurred while closing server socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

}
