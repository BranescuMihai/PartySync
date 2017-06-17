package com.branes.partysync.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.ImageView;

import com.facebook.Profile;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.MODE_PRIVATE;
import static com.branes.partysync.PartySyncApplication.getContext;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class Utilities {

    private static String serviceName = "";

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
        if (!uniqueIds.contains(peerUniqueId)) {
            uniqueIds.add(peerUniqueId);
        }

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

    /**
     * Loads a downloaded image from an Uri into an ImageView
     *
     * @param context    used for Picasso
     * @param pictureUri from which we should start downloading
     * @param imageView  where we put the downloaded picture
     */
    public static void loadRoundedImage(Context context, Uri pictureUri, ImageView imageView) {
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(40)
                .oval(false)
                .build();

        Picasso.with(context)
                .load(pictureUri)
                .fit()
                .transform(transformation)
                .into(imageView);
    }

    /**
     * @return the public Facebook name of the user
     */
    public static String getProfileName() {
        Profile profile = Profile.getCurrentProfile();
        String fbName = "Friend";
        if (profile.getName() != null) {
            fbName = profile.getName();
        }
        return fbName;
    }

    /**
     * @param width  of the image thumbnail
     * @param height of the image thumbnail
     * @return the public Facebook profile picture of the user
     */
    public static Uri getProfilePicture(int width, int height) {
        Profile profile = Profile.getCurrentProfile();
        Uri pictureFromFb = new Uri.Builder().build();
        if (profile.getProfilePictureUri(width, height) != null) {
            pictureFromFb = profile.getProfilePictureUri(width, height);
        }
        return pictureFromFb;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId() {
        String deviceId;
        TelephonyManager mTelephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceId;
    }

    public static BufferedReader getReader(Socket socket) {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ioException) {
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
            if (Constants.DEBUG) {
                ioException.getMessage();
            }
        }
        return null;
    }

    public static void sendInformation(byte[] image, BlockingQueue<byte[]> messageQueue) {
        try {
            messageQueue.put(image);
        } catch (InterruptedException interruptedException) {
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getAllImageNames() {
        File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/partySync");
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();
            ArrayList<String> imageNames = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    imageNames.add(file.getName());
                }
            }
            return imageNames;
        }
        return new ArrayList<>();
    }

    public static ArrayList<File> getAllDifferentImages(List<String> receivedFileNames) {
        File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/partySync");
        if (folder.exists()) {
            File[] listOfFiles = folder.listFiles();

            //get my own file names and remove the ones received
            ArrayList<String> imageNames = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    imageNames.add(file.getName());
                }
            }
            imageNames.removeAll(receivedFileNames);

            //get the files that correspond to the different image names
            ArrayList<File> differentFiles = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile() && imageNames.contains(file.getName())) {
                    differentFiles.add(file);
                }
            }

            return differentFiles;
        }
        return new ArrayList<>();
    }

    public static void setOwnServiceName(String serviceName) {
        Utilities.serviceName = serviceName;
    }

    public static String getOwnServiceName() {
        return serviceName;
    }

    /**
     * Check if app name and group name are identical
     *
     * @param foreignServiceName used to obtain the two required checks
     * @return true if the service name belongs to the same group, false otherwise
     */
    public static boolean checkIfSameGroup(String foreignServiceName) {
        String[] splitOwn = serviceName.split("-");
        String[] splitForeign = foreignServiceName.split("-");

        return splitOwn[0].equals(splitForeign[0]) &&
                splitOwn[2].equals(splitForeign[2]);
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
