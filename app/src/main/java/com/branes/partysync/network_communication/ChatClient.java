package com.branes.partysync.network_communication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.Utilities;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatClient {

    private Socket socket = null;

    private Context context = null;

    private SendThread sendThread = null;
    private ReceiveThread receiveThread = null;

    private BlockingQueue<byte[]> messageQueue = new ArrayBlockingQueue<>(Constants.MESSAGE_QUEUE_CAPACITY);

    public ChatClient(Context context, final String host, final int port) {
        this.context = context;

        try {
            socket = new Socket(host, port);
            Log.d(Constants.TAG, "A socket has been created on " + socket.getInetAddress() + ":" + socket.getLocalPort());
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred while creating the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        if (socket != null) {
            startThreads();
        }
    }

    public ChatClient(Context context, Socket socket) {
        this.context = context;

        this.socket = socket;
        if (socket != null) {
            startThreads();
        }
    }

    public void sendMessage(byte[] image) {
        try {
            messageQueue.put(image);
        } catch (InterruptedException interruptedException) {
            Log.e(Constants.TAG, "An exception has occurred: " + interruptedException.getMessage());
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public void startThreads() {
        sendThread = new SendThread();
        sendThread.start();

        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    public void stopThreads() {

        sendThread.stopThread();
        receiveThread.stopThread();

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred while closing the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread {

        @Override
        public void run() {

            BufferedOutputStream bufferedOutputStream = Utilities.getOutputStream(socket);
            if (bufferedOutputStream != null) {
                try {
                    Log.d(Constants.TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        byte[] content = messageQueue.take();

                        if (content != null) {
                            try {
                                bufferedOutputStream.write(content);
                                bufferedOutputStream.write('\n');
                                bufferedOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (InterruptedException interruptedException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + interruptedException.getMessage());
                    if (Constants.DEBUG) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            Log.i(Constants.TAG, "Send Thread ended");

        }

        public void stopThread() {
            interrupt();
        }

    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {

            BufferedInputStream bufferedInputStream = Utilities.getInputStream(socket);
            if (bufferedInputStream != null) {
                try {
                    Log.d(Constants.TAG, "Reading messages from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        byte[] content = IOUtils.toByteArray(bufferedInputStream);
                        if (content != null) {
                            File photosDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/photos/partySync");
                            if (!photosDirectory.exists()) {
                                photosDirectory.mkdir();
                            }
                            Long tsLong = System.currentTimeMillis() / 1000;
                            String ts = tsLong.toString();
                            FileOutputStream fos = new FileOutputStream(photosDirectory.getPath() + "/" + ts);
                            fos.write(content);
                            fos.close();
                        }
                    }
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }

            Log.i(Constants.TAG, "Receive Thread ended");

        }

        public void stopThread() {
            interrupt();
        }

    }

}
