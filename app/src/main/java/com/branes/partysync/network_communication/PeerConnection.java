package com.branes.partysync.network_communication;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.branes.partysync.actions.AuthenticationFailureActions;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.threads.ReceiveListThread;
import com.branes.partysync.network_communication.threads.ReceiveThread;
import com.branes.partysync.network_communication.threads.SendListThread;
import com.branes.partysync.network_communication.threads.SendThread;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PeerConnection {

    private static final String TAG = PeerConnection.class.getName();

    private Socket socket;

    private SendThread sendThread;
    private ReceiveThread receiveThread;

    private boolean createdFromDiscovery;

    private boolean connectionDeactivated;
    private boolean connectionReady;

    private String peerUniqueId;
    private String profileName;
    private String profilePicture;
    private String foreignServiceName;
    private String host;

    private AuthenticationFailureActions authenticationFailureActions;

    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(Constants.MESSAGE_QUEUE_CAPACITY);

    public PeerConnection(AuthenticationFailureActions authenticationFailureActions, final String host, final int port) {

        this.authenticationFailureActions = authenticationFailureActions;
        this.host = host;

        createdFromDiscovery = true;

        connectionDeactivated = false;

        Handler hand = new Handler();
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port);
                    Log.d(TAG, "A socket has been created on " + socket.getInetAddress() + ":" + socket.getPort());
                } catch (IOException ioException) {
                    Log.e(TAG, "An exception has occurred while creating the socket: " + ioException.getMessage());
                }
                if (socket != null) {
                    startThreads();
                    startListThreads();
                }
            }
        }, 500);
    }

    public PeerConnection(AuthenticationFailureActions authenticationFailureActions, Socket socket) {
        this.authenticationFailureActions = authenticationFailureActions;
        this.socket = socket;

        createdFromDiscovery = false;

        if (socket != null) {
            this.host = socket.getInetAddress().getHostAddress();
            Log.d(TAG, "A socket from server has been created on " + socket.getInetAddress() + ":" + socket.getPort());
            startThreads();
            startListThreads();
        }
    }

    public String getUsername() {
        return profileName;
    }

    public Uri getProfilePictureUri() {
        return Uri.parse(profilePicture);
    }

    public Socket getSocket() {
        return socket;
    }

    public BlockingQueue<byte[]> getMessageQueue() {
        return messageQueue;
    }

    public AuthenticationFailureActions getAuthenticationFailureActions() {
        return authenticationFailureActions;
    }

    public boolean isConnectionReady() {
        return connectionReady;
    }

    public void setConnectionReady(boolean connectionReady) {
        this.connectionReady = connectionReady;
    }

    public String getPeerUniqueId() {
        return peerUniqueId;
    }

    public boolean isConnectionDeactivated() {
        return connectionDeactivated;
    }

    public void setConnectionDeactivated(boolean connectionDeactivated) {
        this.connectionDeactivated = connectionDeactivated;
    }

    public boolean isCreatedFromDiscovery() {
        return createdFromDiscovery;
    }

    public String getForeignServiceName() {
        return foreignServiceName;
    }

    public String getHost() {
        return host;
    }

    private void startThreads() {
        sendThread = new SendThread(this);
        sendThread.start();

        receiveThread = new ReceiveThread(this);
        receiveThread.start();
    }

    private void startListThreads() {
        SendListThread sendListThread = new SendListThread(Utilities.getAllImageNames(), this);
        sendListThread.start();

        ReceiveListThread receiveListThread = new ReceiveListThread(this);
        receiveListThread.start();
    }

    public void stopThreads() {

        sendThread.stopThread();
        receiveThread.stopThread();

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occurred while closing the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void setConnectionDetails(String profileName, String profilePicture, String peerUniqueId, String foreignServiceName) {
        Log.d(TAG, "Auth finished with " + socket.getInetAddress() + ":" + socket.getPort() + "--" + socket.getLocalPort());
        if (Utilities.checkIfSameGroup(foreignServiceName)) {
            ((NetworkServiceManager) authenticationFailureActions).getConnections()
                    .checkForRedundantConnections(host, peerUniqueId);
            this.profileName = profileName;
            this.profilePicture = profilePicture;
            this.peerUniqueId = peerUniqueId;
            this.foreignServiceName = foreignServiceName;
        } else {
            stopThreads();
        }
    }

    public void removeSelfFromConnectionList() {
        ((NetworkServiceManager) authenticationFailureActions).getConnections().removeConnection(this);
    }

    public void compareFiles(List<String> receivedFileNames) throws IOException {

        ArrayList<File> differentImages = Utilities.getAllDifferentImages(receivedFileNames);

        for (File file : differentImages) {
            FileInputStream fileInputStream = new FileInputStream(file);
            int length = (int) file.length();
            byte[] fileContent = new byte[length];
            fileInputStream.read(fileContent, 0, length);

            String[] split = file.getName().split("[_\\.]");
            long timeLong = Long.valueOf(split[1]);
            byte[] timeInBytes = Longs.toByteArray(timeLong);
            byte[] finalBytes = Bytes.concat(timeInBytes, fileContent);

            Utilities.sendInformation(finalBytes, messageQueue);
        }
    }
}
