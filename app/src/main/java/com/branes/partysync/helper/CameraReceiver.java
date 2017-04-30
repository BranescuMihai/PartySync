package com.branes.partysync.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class CameraReceiver extends BroadcastReceiver {

    private static final String TAG = CameraReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String path = IOUtil.getRealPathFromUri(context, intent.getData());
            if (path != null) {
                ObjectObserver.getInstance().updateValue(IOUtil.readFile(path));
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't retrieve picture");
        }
    }
}
