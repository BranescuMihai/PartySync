package com.branes.partysync.helper;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Utilities {

    private static final String TAG = Utilities.class.getName();

    public static BufferedReader getReader(Socket socket) {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occured: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    public static PrintWriter getWriter(Socket socket) {
        try {
            return new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occured: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    public static BufferedInputStream getInputStream(Socket socket) {
        try {
            return new BufferedInputStream(socket.getInputStream());
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occured: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    public static BufferedOutputStream getOutputStream(Socket socket) {
        try {
            return new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            Log.e(TAG, "An exception has occured: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    public static String generateIdentifier(int length) {
        StringBuffer result = new StringBuffer("-");

        Random random = new Random();
        for (int k = 0; k < length; k++) {
            result.append((char) (Constants.FIRST_LETTER + random.nextInt(Constants.ALPHABET_LENGTH)));
        }
        return result.toString();
    }

}
