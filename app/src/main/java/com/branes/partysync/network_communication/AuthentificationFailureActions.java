package com.branes.partysync.network_communication;

import java.net.Socket;

public interface AuthentificationFailureActions {

    /**
     * Remove the corresponding ChatClient from the list
     *
     * @param socket used to uniquely identify the ChatClient
     */
    void authentificationFailed(Socket socket);
}
