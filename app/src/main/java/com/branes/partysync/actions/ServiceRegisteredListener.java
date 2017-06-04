package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface ServiceRegisteredListener {

    /**
     * Set the name of the service
     */
    void onServiceRegistered(NsdServiceInfo nsdServiceInfo);
}
