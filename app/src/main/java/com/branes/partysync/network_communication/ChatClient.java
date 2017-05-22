package com.branes.partysync.network_communication;

import android.os.Environment;
import android.util.Log;

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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.branes.partysync.helper.Utilities.readBytes;
import static com.branes.partysync.helper.Utilities.sendBytes;

public class ChatClient {

    private static final String TAG = ChatClient.class.getName();

    private Socket socket;

    private SendThread sendThread;
    private ReceiveThread receiveThread;

    private OutputStream outputStream;
    private InputStream inputStream;

    private AuthentificationFailureActions authentificationFailureActions;

    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(Constants.MESSAGE_QUEUE_CAPACITY);

    ChatClient(AuthentificationFailureActions authentificationFailureActions, final String host, final int port) {

        this.authentificationFailureActions = authentificationFailureActions;

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

    public ChatClient(AuthentificationFailureActions authentificationFailureActions, Socket socket) {
        this.authentificationFailureActions = authentificationFailureActions;
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

    Socket getSocket() {
        return socket;
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
                sendInformation(SecurityHelper.encryptMsg("Valid password"));
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                    | IllegalBlockSizeException | InvalidParameterSpecException
                    | BadPaddingException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (outputStream != null) {
                try {
                    Log.d(TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        byte[] content = messageQueue.take();
                        if (content != null) {
                            try {
                                sendBytes(content, outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
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

            boolean authentificationCompleted = false;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                try {
                    Log.d(TAG, "Reading messages from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        byte[] content = readBytes(inputStream);
                        if (content != null) {

                            if (!authentificationCompleted) {
                                try {
                                    if (SecurityHelper.decryptMsg(content).equals("Valid password")) {
                                        authentificationCompleted = true;
                                    } else {
                                        authentificationFailureActions.authentificationFailed(socket);
                                        stopThreads();
                                    }

                                } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                                        | IllegalBlockSizeException | InvalidParameterSpecException
                                        | BadPaddingException | UnsupportedEncodingException
                                        | InvalidAlgorithmParameterException e) {
                                    e.printStackTrace();
                                }
                            }

                            File photosDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/photos/partySync");

                            if (photosDirectory.exists() || photosDirectory.mkdirs()) {
                                Long tsLong = System.currentTimeMillis() / 1000;
                                String ts = tsLong.toString();

                                File pictureFile = new File(photosDirectory, "image" + ts);

                                if (pictureFile.exists() || pictureFile.createNewFile()) {
                                    FileOutputStream fos = new FileOutputStream(pictureFile);
                                    fos.write(content);
                                    fos.close();
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
}
