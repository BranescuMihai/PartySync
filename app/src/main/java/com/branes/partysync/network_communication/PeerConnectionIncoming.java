package com.branes.partysync.network_communication;

import android.util.Log;

import com.branes.partysync.PartySyncApplication;
import com.branes.partysync.dependency_injection.DependencyInjection;
import com.branes.partysync.helper.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.inject.Inject;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PeerConnectionIncoming extends Thread {

    private static final String TAG = PeerConnectionIncoming.class.getName();

    @Inject
    NetworkServiceManager networkServiceManager;

    private ServerSocket serverSocket = null;

    PeerConnectionIncoming() {

        DependencyInjection.getAppComponent(PartySyncApplication.getContext()).inject(this);

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
                Log.i(TAG, "Received a connection request from: " + socket.getInetAddress() + ":" + socket.getPort());
                networkServiceManager.getConnections().addConnection(networkServiceManager, socket);
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
