package com.branes.partysync.activities.main;

import android.content.Context;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
interface MainContract {

    interface View {
        Context getContext();
    }

    interface Presenter {
        void startJobScheduler();

        void stopJobScheduler();
    }
}
