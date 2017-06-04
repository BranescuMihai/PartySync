package com.branes.partysync.network_communication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.branes.partysync.PartySyncApplication;
import com.branes.partysync.actions.AuthenticationFailureActions;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.SecurityHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.content.Context.MODE_PRIVATE;
import static com.branes.partysync.helper.Utilities.readBytes;
import static com.branes.partysync.helper.Utilities.sendBytes;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PeerConnection {

    private static final String TAG = PeerConnection.class.getName();

    private Socket socket;

    private SendThread sendThread;
    private ReceiveThread receiveThread;

    private OutputStream outputStream;
    private InputStream inputStream;

    private boolean connectionDeactivated;

    private String peerUniqueId;
    private String username;
    private String host;

    private AuthenticationFailureActions authenticationFailureActions;

    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(Constants.MESSAGE_QUEUE_CAPACITY);

    PeerConnection(AuthenticationFailureActions authenticationFailureActions, final String host, final int port, String username) {

        this.authenticationFailureActions = authenticationFailureActions;
        this.username = username;
        this.host = host;

        connectionDeactivated = false;

        try {
            socket = new Socket(host, port);
            Log.d(TAG, "A socket has been created on " + socket.getInetAddress() + ":" + socket.getLocalPort());
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occurred while creating the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        if (socket != null) {
            startThreads();
        }
    }

    PeerConnection(AuthenticationFailureActions authenticationFailureActions, Socket socket) {
        this.authenticationFailureActions = authenticationFailureActions;
        this.socket = socket;

        if (socket != null) {
            startThreads();
        }
    }

    public void sendInformation(byte[] image) {
        try {
            messageQueue.put(image);
        } catch (InterruptedException interruptedException) {
            Log.e(TAG, "An exception has occurred: " + interruptedException.getMessage());
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    String getHost() {
        return host;
    }

    Socket getSocket() {
        return socket;
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

    private void startThreads() {
        sendThread = new SendThread();
        sendThread.start();

        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    void stopThreads() {

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

    private class SendThread extends Thread {

        @Override
        public void run() {

            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sendInformation(SecurityHelper.encryptMsg("Valid:" + getDeviceId()));
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                    | IllegalBlockSizeException | InvalidParameterSpecException | BadPaddingException
                    | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }

            if (outputStream != null) {
                try {
                    Log.d(TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        if(!isConnectionDeactivated()) {
                            byte[] content = messageQueue.take();
                            if (content != null) {
                                try {
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

        void stopThread() {
            interrupt();
        }

    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {

            boolean authenticationCompleted = false;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                try {
                    Log.d(TAG, "Reading messages from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        if(!isConnectionDeactivated()) {
                            byte[] content = readBytes(inputStream);
                            if (content != null) {

                                if (!authenticationCompleted) {
                                    try {
                                        String decrypted = SecurityHelper.decryptMsg(content);
                                        if (decrypted.contains("Valid")) {

                                            String[] split = decrypted.split(":");
                                            peerUniqueId = split[1];
                                            saveUniqueIdInSharedPreferences(peerUniqueId);
                                            authenticationCompleted = true;
                                            continue;
                                        }
                                    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                                            | IllegalBlockSizeException | InvalidParameterSpecException
                                            | BadPaddingException | UnsupportedEncodingException
                                            | InvalidAlgorithmParameterException e) {
                                        authenticationFailureActions.onAuthenticationFailed(socket);
                                        stopThreads();
                                        continue;
                                    }
                                }

                                File photosDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/partySync");

                                if (photosDirectory.exists() || photosDirectory.mkdirs()) {
                                    Long tsLong = System.currentTimeMillis() / 1000;
                                    String ts = tsLong.toString();

                                    File pictureFile = new File(photosDirectory, "image" + ts + ".jpg");

                                    if (pictureFile.exists() || pictureFile.createNewFile()) {
                                        FileOutputStream fos = new FileOutputStream(pictureFile);
                                        fos.write(content);
                                        fos.close();
                                    }
                                }
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

        void stopThread() {
            interrupt();
        }
    }

    @SuppressLint("HardwareIds")
    private String getDeviceId() {
        String deviceId;
        TelephonyManager mTelephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    private Context getContext() {
        return PartySyncApplication.getContext();
    }

    private void saveUniqueIdInSharedPreferences(String peerUniqueId) {
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.SERVICE_NAME, MODE_PRIVATE);

        Set<String> uniqueIds = prefs.getStringSet("peerUniqueIds", null);

        if (uniqueIds == null) {
            uniqueIds = new HashSet<>();
        }
        uniqueIds.add(peerUniqueId);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("peerUniqueIds", uniqueIds);
        editor.apply();
    }
}
