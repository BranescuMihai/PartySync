package com.branes.partysync.activities.peers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.branes.partysync.R;
import com.branes.partysync.actions.PeerElementActions;
import com.branes.partysync.dependency_injection.DependencyInjection;
import com.branes.partysync.helper.Utilities;
import com.branes.partysync.network_communication.NetworkServiceManager;
import com.branes.partysync.network_communication.PeerConnection;

import java.util.List;

import javax.inject.Inject;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PeerActivity extends AppCompatActivity implements PeerElementActions {

    private List<PeerConnection> peerConnections;

    @Inject
    NetworkServiceManager networkServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DependencyInjection.getAppComponent(this).inject(this);

        setContentView(R.layout.activity_peer);

        ListView peerList = (ListView) findViewById(R.id.peer_list);

        peerConnections = networkServiceManager.getCommunicationToPeers();

        PeerAdapter peerAdapter = new PeerAdapter(this, peerConnections, Utilities.getUniqueIdFromSharedPreferences(this));
        peerList.setAdapter(peerAdapter);
    }

    @Override
    public void onPeerElementClicked(int position, boolean isEnabled) {
        String selectedId = peerConnections.get(position).getPeerUniqueId();

        if (isEnabled) {
            peerConnections.get(position).setConnectionDeactivated(false);
            Utilities.removeUniqueIdFromSharedPreferences(this, selectedId);
        } else {
            peerConnections.get(position).setConnectionDeactivated(true);
            Utilities.saveUniqueIdInSharedPreferences(this, selectedId);
        }
    }
}
