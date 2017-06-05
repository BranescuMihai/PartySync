package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface ServiceResolvedActions {

    /**
     * Transmit to the manager that a new service was resolved
     */
    void onServiceResolved(NsdServiceInfo serviceInfo);

    /**
     * Transmit to the manager that a service couldn't be resolved, so it can retry
     */
    void onServiceFailed(NsdServiceInfo serviceInfo);
}
