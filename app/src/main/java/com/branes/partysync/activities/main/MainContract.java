package com.branes.partysync.activities.main;

import android.content.Context;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
interface MainContract {

    interface View {

        /**
         * @return the context from the activity
         */
        Context getContext();

        /**
         * Set the number o peers in the Textview when a change happens
         */
        void setNumberOfPeers(String numberOfPeers);

        /**
         * @return the string from the username Edittext
         */
        String getUsername();
    }

    interface Presenter {

        /**
         * Start the job scheduler and register the nsd peer
         */
        void startServices();

        /**
         * Stop the job scheduler and un-register the nsd peer
         */
        void stopServices();

        /**
         * @return true if the manager has peers connected, false otherwise
         */
        boolean arePeersConnected();
    }
}
