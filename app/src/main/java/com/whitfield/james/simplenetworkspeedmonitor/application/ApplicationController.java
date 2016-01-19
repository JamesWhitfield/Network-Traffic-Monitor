package com.whitfield.james.simplenetworkspeedmonitor.application;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.whitfield.james.simplenetworkspeedmonitor.BuildConfig;
import com.whitfield.james.simplenetworkspeedmonitor.R;

/**
 * Created by jwhit on 19/01/2016.
 */
public class ApplicationController extends Application {

    public static final String BUILD_TYPE_DEBUG = "debug";
    public static final String BUILD_TYPE_RELEASE = "release";

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
            if(BuildConfig.BUILD_TYPE.equals(BUILD_TYPE_DEBUG)){
                analytics.setDryRun(true);
            }
        }
        return mTracker;
    }

}
