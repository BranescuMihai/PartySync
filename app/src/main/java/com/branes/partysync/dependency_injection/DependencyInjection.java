package com.branes.partysync.dependency_injection;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Copyright (c) 2017 Mihai Branescu
 */
public final class DependencyInjection {

    public static AppComponent getAppComponent(@NonNull Context context) {
        return ((AppComponentProvider) context.getApplicationContext()).getComponent();
    }

}