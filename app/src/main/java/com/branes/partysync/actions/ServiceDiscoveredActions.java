package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface ServiceDiscoveredActions {

    /**
     * Transmit to the manager that a new service has been found
     */
    void onServiceFound(NsdServiceInfo nsdServiceInfo);

    /**
     * Transmit to the manager that a service can't be reached anymore
     */
    void onServiceLost(NsdServiceInfo nsdServiceInfo);
}
