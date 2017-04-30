package com.branes.partysync.helper;

import android.os.FileObserver;
import android.util.Log;

/**
 * Copyright (c) 2017 Mihai Branescu
 */

@Deprecated
public class GalleryObserver extends FileObserver {
    static final String TAG = "FILEOBSERVER";
    /**
     * should end with "/"
     */
    private String rootPath;
    static final int mask = (FileObserver.CREATE |
            FileObserver.DELETE |
            FileObserver.DELETE_SELF);

    public GalleryObserver(String root) {
        super(root, mask);
        rootPath = root;
    }

    public void onEvent(int event, String path) {

        switch (event) {
            case FileObserver.CREATE:
                Log.d(TAG, "CREATE:" + rootPath + path);
                break;
            case FileObserver.DELETE:
                Log.d(TAG, "DELETE:" + rootPath + path);
                break;
            case FileObserver.DELETE_SELF:
                Log.d(TAG, "DELETE_SELF:" + rootPath + path);
        }
    }

    public void close() {
        super.finalize();
    }
}