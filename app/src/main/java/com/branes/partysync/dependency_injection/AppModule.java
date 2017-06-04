package com.branes.partysync.dependency_injection;

import android.content.Context;
import android.content.SharedPreferences;

import com.branes.partysync.helper.Constants;
import com.branes.partysync.network_communication.NetworkServiceDiscoveryOperations;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Copyright © 2017 Deutsche Bank. All rights reserved.
 */
@Module
public class AppModule {

    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    Context provideContext() {
        return context;
    }

    @Provides
    SharedPreferences provideSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.SERVICE_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    NetworkServiceDiscoveryOperations provideNetworkServiceDiscoveryOperations(Context context) {
        return new NetworkServiceDiscoveryOperations(context);
    }
}