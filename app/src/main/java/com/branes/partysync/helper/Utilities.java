package com.branes.partysync.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class Utilities {

    public static String generateIdentifier(int length) {
        StringBuilder result = new StringBuilder("-");

        Random random = new Random();
        for (int k = 0; k < length; k++) {
            result.append((char) (Constants.FIRST_LETTER + random.nextInt(Constants.ALPHABET_LENGTH)));
        }
        result.append("-");

        return result.toString();
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        DataInputStream dis = new DataInputStream(inputStream);

        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    public static void sendBytes(byte[] myByteArray, OutputStream outputStream) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length, outputStream);
    }

    public static Set<String> getUniqueIdFromSharedPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SERVICE_NAME, MODE_PRIVATE);

        Set<String> uniqueIds = prefs.getStringSet("peerUniqueIds", null);

        if (uniqueIds == null) {
            uniqueIds = new HashSet<>();
        }

        return uniqueIds;
    }

    public static void saveUniqueIdInSharedPreferences(Context context, String peerUniqueId) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SERVICE_NAME, MODE_PRIVATE);

        Set<String> uniqueIds = getUniqueIdFromSharedPreferences(context);
        uniqueIds.add(peerUniqueId);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("peerUniqueIds", uniqueIds);
        editor.apply();
    }

    public static void removeUniqueIdFromSharedPreferences(Context context, String peerUniqueId) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SERVICE_NAME, MODE_PRIVATE);

        Set<String> uniqueIds = getUniqueIdFromSharedPreferences(context);
        if (uniqueIds.contains(peerUniqueId)) {
            uniqueIds.remove(peerUniqueId);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("peerUniqueIds", uniqueIds);
        editor.apply();
    }

    private static void sendBytes(byte[] myByteArray, int start, int len, OutputStream outputStream) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);

        DataOutputStream dos = new DataOutputStream(outputStream);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }
}
