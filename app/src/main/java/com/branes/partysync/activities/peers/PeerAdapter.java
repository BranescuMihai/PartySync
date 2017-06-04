package com.branes.partysync.activities.peers;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.branes.partysync.R;
import com.branes.partysync.actions.PeerElementActions;
import com.branes.partysync.network_communication.PeerConnection;

import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
class PeerAdapter extends BaseAdapter {

    private List<PeerConnection> peerConnections;
    private Set<String> restrictedPeers;
    private PeerElementActions peerElementActions;
    private int position;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            peerElementActions.onPeerElementClicked(position, isChecked);
        }
    };

    PeerAdapter(PeerElementActions peerElementActions, List<PeerConnection> peerConnections, Set<String> restrictedPeers) {
        this.peerConnections = peerConnections;
        this.restrictedPeers = restrictedPeers;
        this.peerElementActions = peerElementActions;
    }

    @Override
    public int getCount() {
        return peerConnections.size();
    }

    @Override
    public Object getItem(int i) {
        return peerConnections.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        this.position = position;
        final PeerViewHolder holder;

        PeerConnection peerConnection = peerConnections.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.peer_element, parent, false);
            holder = new PeerViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (PeerViewHolder) convertView.getTag();
        }

        holder.peerUsername.setText(peerConnection.getUsername());

        if (restrictedPeers != null && restrictedPeers.contains(peerConnection.getPeerUniqueId())) {
            setCheckedAutomatically(holder.peerSwitch, false);
        } else {
            setCheckedAutomatically(holder.peerSwitch, true);
        }

        holder.peerSwitch.setOnCheckedChangeListener(onCheckedChangeListener);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = holder.peerSwitch.isChecked();
                holder.peerSwitch.setChecked(!checked);
            }
        });

        return convertView;
    }

    private static class PeerViewHolder {
        TextView peerUsername;
        Switch peerSwitch;
        RelativeLayout container;

        PeerViewHolder(View itemView) {
            peerUsername = (TextView) itemView.findViewById(R.id.peer_item_text);
            peerSwitch = (Switch) itemView.findViewById(R.id.peer_item_switch);
            container = (RelativeLayout) itemView.findViewById(R.id.peer_item_container);
        }
    }

    private void setCheckedAutomatically(Switch peerSwitch, boolean isEnabled) {
        peerSwitch.setOnCheckedChangeListener(null);
        peerSwitch.setChecked(isEnabled);
        peerSwitch.jumpDrawablesToCurrentState();
        peerSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
