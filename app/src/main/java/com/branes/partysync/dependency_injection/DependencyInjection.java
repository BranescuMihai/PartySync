package com.branes.partysync.dependency_injection;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Copyright © 2017 Deutsche Bank. All rights reserved.
 */
public final class DependencyInjection {

    public static AppComponent getAppComponent(@NonNull Context context) {
        return ((AppComponentProvider) context.getApplicationContext()).getComponent();
    }

}