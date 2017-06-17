package com.branes.partysync.network_communication.threads;

import android.util.Log;

import com.branes.partysync.helper.Constants;
import com.branes.partysync.network_communication.PeerConnection;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.branes.partysync.helper.Utilities.sendBytes;

/**
 * Copyright © 2017 Deutsche Bank. All rights reserved.
 */
public class SendThread extends Thread {

    private static final String TAG = SendThread.class.getName();

    private PeerConnection peerConnection;
    private Socket socket;

    public SendThread(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
        this.socket = peerConnection.getSocket();
    }

    @Override
    public void run() {

        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (outputStream != null) {
            try {
                Log.d(TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getPort());
                while (!Thread.currentThread().isInterrupted()) {
                    if (!peerConnection.isConnectionDeactivated() && peerConnection.isConnectionReady()) {
                        byte[] content = peerConnection.getMessageQueue().take();
                        if (content != null) {
                            try {
                                Log.d(TAG, "SEND image number: " + String.valueOf(Longs.fromByteArray(content))
                                        + " to " + socket.getInetAddress() + ":" + socket.getPort());
                                sendBytes(content, outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (InterruptedException interruptedException) {
                Log.e(TAG, "An exception has occurred: " + interruptedException.getMessage());
                if (Constants.DEBUG) {
                    interruptedException.printStackTrace();
                }
            }
        }

        Log.i(TAG, "Send Thread ended");

    }

    public void stopThread() {
        interrupt();
    }
}