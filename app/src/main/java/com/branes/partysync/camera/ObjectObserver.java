package com.branes.partysync.camera;

import java.util.Observable;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class ObjectObserver extends Observable {

    private static ObjectObserver instance = new ObjectObserver();

    public static ObjectObserver getInstance() {
        return instance;
    }

    private ObjectObserver() {
    }

    void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}