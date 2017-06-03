package com.branes.partysync.actions;


public interface PeerElementActions {

    /**
     * Used to determine which peer should be restricted/de-restricted
     *
     * @param position  which was selected
     * @param isEnabled used to know what state should the peer be
     */
    void onPeerElementClicked(int position, boolean isEnabled);
}
