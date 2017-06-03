package com.branes.partysync.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.branes.partysync.helper.IoUtilities;

import java.io.IOException;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class CameraReceiver extends BroadcastReceiver {

    private static final String TAG = CameraReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String path = IoUtilities.getRealPathFromUri(context, intent.getData());
            if (path != null) {
                ObjectObserver.getInstance().updateValue(IoUtilities.readFile(path));
            }
        } catch (IOException e) {
            Log.e(TAG, "Couldn't retrieve picture");
        }
    }
}
