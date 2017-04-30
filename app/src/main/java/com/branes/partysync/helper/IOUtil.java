package com.branes.partysync.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Copyright (c) 2017 Mihai Branescu
 */

public class IOUtil {

    private static final String TAG = IOUtil.class.getName();

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    private static byte[] readFile(File file) throws IOException {

        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            long longLength = f.length();
            int length = (int) longLength;
            if (length != longLength)
                throw new IOException("File size >= 2 GB");

            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index;
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if(cursor.moveToFirst())
                    return cursor.getString(column_index);
                else
                    return "";
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}