package com.branes.partysync.actions;

import android.net.nsd.NsdServiceInfo;


public interface ServiceRegisteredListener {

    /**
     * Set the name of the service
     */
    void onServiceRegistered(NsdServiceInfo nsdServiceInfo);
}
