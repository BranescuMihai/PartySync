package com.branes.partysync.actions;

import java.net.Socket;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public interface AuthenticationFailureActions {

    /**
     * Remove the corresponding PeerConnection from the list
     *
     * @param socket used to uniquely identify the PeerConnection
     */
    void onAuthenticationFailed(Socket socket);
}
