package com.branes.partysync.network_communication;

import android.util.Log;

import com.branes.partysync.helper.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ChatServer extends Thread {

    private static final String TAG = ChatServer.class.getName();
    
    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private ServerSocket serverSocket = null;

    public ChatServer(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations, int port) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
        try {
            serverSocket = new ServerSocket(port);
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
                List<ChatClient> communicationFromClients = networkServiceDiscoveryOperations.getCommunicationFromClients();
                communicationFromClients.add(new ChatClient(null, socket));
                networkServiceDiscoveryOperations.setCommunicationFromClients(communicationFromClients);
            }
        } catch (IOException ioException) {
            Log.e(TAG, "An error has occurred during server run: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void stopThread() {
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
