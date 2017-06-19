package com.branes.partysync.network_communication.threads;

import android.util.Log;

import com.branes.partysync.helper.SecurityHelper;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.PeerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.branes.partysync.helper.Utilities.readBytes;


public class ReceiveListThread extends Thread {

    private static final String TAG = ReceiveListThread.class.getName();

    private Socket socketForList;
    private PeerConnection peerConnection;

    public ReceiveListThread(PeerConnection peerConnection) {
        this.socketForList = peerConnection.getSocket();
        this.peerConnection = peerConnection;
    }

    @Override
    public void run() {
        boolean authenticationCompleted = false;
        BufferedReader bufferedReader = Utilities.getReader(socketForList);
        InputStream inputStream = null;
        try {
            inputStream = socketForList.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bufferedReader != null && !peerConnection.isConnectionDeactivated()) {
            String content = "";

            while (!Thread.currentThread().isInterrupted()) {

                if (!authenticationCompleted) {
                    if (inputStream != null) {
                        byte[] authInfo = new byte[0];
                        try {
                            authInfo = readBytes(inputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                            peerConnection.removeSelfFromConnectionList();
                            interrupt();
                        }
                        if (authInfo != null) {
                            try {
                                String decrypted = SecurityHelper.decryptMsg(authInfo);
                                if (decrypted.contains("Valid")) {

                                    String[] split = decrypted.split("::");
                                    peerConnection.setConnectionDetails(split[1], split[2], split[3], split[4]);
                                    authenticationCompleted = true;
                                    continue;
                                }
                            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                                    | IllegalBlockSizeException | InvalidParameterSpecException
                                    | BadPaddingException | UnsupportedEncodingException
                                    | InvalidAlgorithmParameterException e) {
                                peerConnection.getAuthenticationFailureActions().onAuthenticationFailed(socketForList);
                                interrupt();
                            }
                        }
                    }
                }

                try {
                    content = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (content != null) {
                    Log.d(TAG, "Initial receiving messages from " + socketForList.getInetAddress() + ":" + socketForList.getPort());
                    peerConnection.setConnectionReady(true);
                    if (!content.equals("Empty")) {
                        String[] split = content.split("\\|");
                        if (split.length != 0) {
                            try {
                                peerConnection.compareFiles(Arrays.asList(split));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    interrupt();
                }
            }
        }
    }
}