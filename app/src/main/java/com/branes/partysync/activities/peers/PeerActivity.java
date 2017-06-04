package com.branes.partysync.activities.peers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.branes.partysync.R;
import com.branes.partysync.actions.PeerElementActions;
import com.branes.partysync.helper.Constants;
import com.branes.partysync.helper.Utilities;

import java.util.ArrayList;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public class PeerActivity extends AppCompatActivity implements PeerElementActions {

    private ArrayList<String> uniqueIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer);

        ListView peerList = (ListView) findViewById(R.id.peer_list);

        ArrayList<String> names = getIntent().getStringArrayListExtra(Constants.CLIENT_NAMES);
        uniqueIds = getIntent().getStringArrayListExtra(Constants.CLIENT_IDS);

        PeerAdapter peerAdapter = new PeerAdapter(this, names, uniqueIds, Utilities.getUniqueIdFromSharedPreferences(this));
        peerList.setAdapter(peerAdapter);
    }

    @Override
    public void onPeerElementClicked(int position, boolean isEnabled) {
        String selectedId = uniqueIds.get(position);

        if(isEnabled) {
            Utilities.removeUniqueIdFromSharedPreferences(this, selectedId);
        } else {
            Utilities.saveUniqueIdInSharedPreferences(this, selectedId);
        }
    }
}
