package com.branes.partysync.network_communication.threads;

import android.util.Log;

import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.IoUtilities;
import com.branes.partysync.network_communication.PeerConnection;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import static com.branes.partysync.helper.Utilities.readBytes;


public class ReceiveThread extends Thread {

    private static final String TAG = ReceiveThread.class.getName();

    private PeerConnection peerConnection;
    private Socket socket;

    public ReceiveThread(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
        this.socket = peerConnection.getSocket();
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            try {
                Log.d(TAG, "Reading messages from " + socket.getInetAddress() + ":" + socket.getPort());
                while (!Thread.currentThread().isInterrupted()) {
                    if (!peerConnection.isConnectionDeactivated() && !socket.isClosed() && peerConnection.isConnectionReady()) {
                        byte[] content = readBytes(inputStream);
                        if (content != null) {
                            String timeString = String.valueOf(Longs.fromByteArray(content));
                            Log.d(TAG, "READ image number: " + timeString + " from "
                                    + socket.getInetAddress() + ":" + socket.getPort());
                            byte[] contentWithoutTime = Arrays.copyOfRange(content, 8, content.length);

                            IoUtilities.createFileFromImage(peerConnection.getUsername().replaceAll("\\s+", ""),
                                    timeString, contentWithoutTime);
                        }
                    }
                }
            } catch (IOException ioException) {
                Log.e(TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }

        Log.i(TAG, "Receive Thread ended");
    }

    public void stopThread() {
        interrupt();
    }
}