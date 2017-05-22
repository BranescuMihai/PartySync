package com.branes.partysync.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class Utilities {

    public static String generateIdentifier(int length) {
        StringBuffer result = new StringBuffer("-");

        Random random = new Random();
        for (int k = 0; k < length; k++) {
            result.append((char) (Constants.FIRST_LETTER + random.nextInt(Constants.ALPHABET_LENGTH)));
        }
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
