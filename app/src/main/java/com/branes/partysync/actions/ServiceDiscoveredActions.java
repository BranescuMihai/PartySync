package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;

public interface ServiceDiscoveredActions {

    void onServiceFound(NsdServiceInfo nsdServiceInfo);

    void onServiceLost(NsdServiceInfo nsdServiceInfo);
}
