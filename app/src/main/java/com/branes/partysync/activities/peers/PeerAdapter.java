package com.branes.partysync.activities.peers;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.branes.partysync.R;
import com.branes.partysync.actions.PeerElementActions;

import java.util.ArrayList;
import java.util.Set;


class PeerAdapter extends BaseAdapter {

    private ArrayList<String> peerNames;
    private ArrayList<String> peerIds;
    private Set<String> restrictedPeers;
    private PeerElementActions peerElementActions;
    private int position;

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            peerElementActions.onPeerElementClicked(position, isChecked);
        }
    };

    PeerAdapter(PeerElementActions peerElementActions, ArrayList<String> peerNames,
                ArrayList<String> peerIds, Set<String> restrictedPeers) {
        this.peerIds = peerIds;
        this.peerNames = peerNames;
        this.restrictedPeers = restrictedPeers;
        this.peerElementActions = peerElementActions;
    }

    @Override
    public int getCount() {
        return peerNames.size();
    }

    @Override
    public Object getItem(int i) {
        return peerNames.get(i);
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

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.peer_element, parent, false);
            holder = new PeerViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (PeerViewHolder) convertView.getTag();
        }

        holder.peerUsername.setText(peerNames.get(position));

        if (restrictedPeers != null && restrictedPeers.contains(peerIds.get(position))) {
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
        LinearLayout container;

        PeerViewHolder(View itemView) {
            peerUsername = (TextView) itemView.findViewById(R.id.peer_item_text);
            peerSwitch = (Switch) itemView.findViewById(R.id.peer_item_switch);
            container = (LinearLayout) itemView.findViewById(R.id.peer_item_container);
        }
    }

    private void setCheckedAutomatically(Switch peerSwitch, boolean isEnabled) {
        peerSwitch.setOnCheckedChangeListener(null);
        peerSwitch.setChecked(isEnabled);
        peerSwitch.jumpDrawablesToCurrentState();
        peerSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
