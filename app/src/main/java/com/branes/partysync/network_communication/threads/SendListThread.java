package com.branes.partysync.network_communication.threads;

import android.util.Log;

import com.branes.partysync.helper.SecurityHelper;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.PeerConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.branes.partysync.helper.Utilities.sendBytes;


public class SendListThread extends Thread {

    private static final String TAG = SendListThread.class.getName();

    private List<String> imageNames;
    private Socket socketForList;
    private PeerConnection peerConnection;

    public SendListThread(List<String> imageNames, PeerConnection peerConnection) {
        this.imageNames = imageNames;
        this.socketForList = peerConnection.getSocket();
        this.peerConnection = peerConnection;
    }

    @Override
    public void run() {

        /* START SENDING THE AUTH INFO */
        OutputStream outputStream = null;
        try {
            outputStream = socketForList.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (outputStream != null) {
            try {
                sendBytes(SecurityHelper.encryptMsg("Valid::" + Utilities.getProfileName() + "::"
                        + Utilities.getProfilePicture(120, 120).toString() + "::" + Utilities.getDeviceId() + "::" +
                        Utilities.getOwnServiceName()), outputStream);

            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                    | IllegalBlockSizeException | InvalidParameterSpecException | BadPaddingException
                    | InvalidAlgorithmParameterException | IOException e) {
                e.printStackTrace();
            }
        }
        /* END SENDING THE AUTH INFO */

        /* START SENDING THE LIST OF IMAGES */
        PrintWriter printWriter = Utilities.getWriter(socketForList);
        if (printWriter != null && !peerConnection.isConnectionDeactivated()) {

            StringBuilder concatenatedBuilder = new StringBuilder();
            for (String image : imageNames) {
                concatenatedBuilder
                        .append(image)
                        .append("|");
            }
            if (!concatenatedBuilder.toString().isEmpty() && concatenatedBuilder.charAt(concatenatedBuilder.length() - 1) == '|') {
                concatenatedBuilder.deleteCharAt(concatenatedBuilder.length() - 1);
            }

            String concatenated = concatenatedBuilder.toString();
            if (!concatenated.isEmpty()) {
                Log.d(TAG, "Initial sending messages to " + socketForList.getInetAddress() + ":" + socketForList.getPort());
                printWriter.println(concatenated);
                printWriter.flush();
            } else {
                Log.d(TAG, "Initial sending empty messages to " + socketForList.getInetAddress() + ":" + socketForList.getPort());
                printWriter.println("Empty");
                printWriter.flush();
            }
        }
        /* END SENDING THE LIST OF IMAGES */
    }
}