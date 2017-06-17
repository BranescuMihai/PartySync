package com.branes.partysync.model;

import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.NetworkServiceManager;
import com.branes.partysync.network_communication.PeerConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Connections {

    private List<PeerConnection> peerConnections;

    public Connections() {
        peerConnections = new CopyOnWriteArrayList<>();
    }

    public List<PeerConnection> getPeerConnections() {
        return peerConnections;
    }

    public int getSize() {
        return peerConnections.size();
    }

    /**
     * Add a new connection
     *
     * @param networkServiceManager used for the authentication actions
     * @param socket                obtained from the server socket accept
     * @return true if a new connection was added, false otherwise
     */
    public boolean addConnection(NetworkServiceManager networkServiceManager, Socket socket) {
        return peerConnections.add(new PeerConnection(networkServiceManager, socket));
    }

    /**
     * Add a new connection
     *
     * @param networkServiceManager used for the authentication actions
     * @param host                  used to create a new socket
     * @param port                  used to create a new socket
     * @return true if a new connection was added, false otherwise
     */
    public boolean addConnection(NetworkServiceManager networkServiceManager, String host, int port) {
        return peerConnections.add(new PeerConnection(networkServiceManager, host, port));
    }

    /**
     * Each side of the connection performs this operation. The peer with the bigger device id
     * closes the discovered connection, the other one the server one. This removal is done only if the
     * number of connections is bigger than one.
     *
     * @param host     used to identify the peer in the list
     * @param deviceId used to determine which connection to remove
     */
    public void checkForRedundantConnections(String host, String deviceId) {
        boolean deleteDiscoveredConnection = false;

        if (deviceId.compareTo(Utilities.getDeviceId()) > 0) {
            deleteDiscoveredConnection = true;
        }

        int numberOfConnectionsWithThisHost = 0;

        for (PeerConnection peer : peerConnections) {
            if (peer.getHost().equals(host)) {
                numberOfConnectionsWithThisHost++;
            }
        }

        if (numberOfConnectionsWithThisHost >= 2) {
            for (PeerConnection peer : peerConnections) {
                if (peer.getHost().equals(host)) {
                    if ((deleteDiscoveredConnection && peer.isCreatedFromDiscovery())
                            || (!deleteDiscoveredConnection && !peer.isCreatedFromDiscovery())) {
                        closeConnection(peer);
                        peerConnections.remove(peer);
                    }
                }
            }
        }
    }

    /**
     * Close all sockets and remove all connections from the list
     */
    public void clearConnections() {
        for (PeerConnection peer : peerConnections) {
            closeConnection(peer);
        }
        peerConnections.clear();
    }

    /**
     * Remove a connection from the list if it exists, identified by serviceName
     *
     * @param serviceName used to identify the connection
     * @return true if the connection was removed, false otherwise
     */
    public boolean removeConnection(String serviceName) {
        for (PeerConnection peer : peerConnections) {
            if (peer.getForeignServiceName() != null &&
                    peer.getForeignServiceName().equals(serviceName)) {
                closeConnection(peer);
                return peerConnections.remove(peer);
            }
        }
        return false;
    }

    /**
     * Remove a connection from the list if it exists, identified by socket
     *
     * @param socket used to identify the connection
     * @return true if the connection was removed, false otherwise
     */
    public boolean removeConnection(Socket socket) {
        for (PeerConnection peer : peerConnections) {
            if (peer.getSocket() != null &&
                    peer.getSocket().equals(socket)) {
                closeConnection(peer);
                return peerConnections.remove(peer);
            }
        }
        return false;
    }

    /**
     * Close a connection's socket and stop its threads
     *
     * @param peerConnection - which connection to close
     */
    private void closeConnection(PeerConnection peerConnection) {
        try {
            peerConnection.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        peerConnection.stopThreads();
    }
}
