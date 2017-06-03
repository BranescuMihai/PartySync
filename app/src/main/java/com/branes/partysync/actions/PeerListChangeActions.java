package com.branes.partysync.actions;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface PeerListChangeActions {

    /**
     * Used to update the number of peers displayed in the circular text view
     */
    void onPeerListChanged();
}
