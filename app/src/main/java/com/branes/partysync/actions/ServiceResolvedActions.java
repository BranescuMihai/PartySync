package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface ServiceResolvedActions {

    void onServiceResolved(NsdServiceInfo serviceInfo, String serviceName);
}
