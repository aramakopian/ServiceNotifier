package com.example.aram.servicenotifier.notifier.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.telephony.ServiceState;
import android.util.Log;

import com.example.aram.servicenotifier.R;
import com.example.aram.servicenotifier.infrastructure.MyApp;
import com.example.aram.servicenotifier.notifier.service.SignalMonitorService;

/**
 * Class AlertCriteria
 */
public class AlertCriteria {

    // Service states: IN_SERVICE, OUT_OF_SERVICE, EMERGENCY_ONLY, POWER_OFF

    public static int persistenceDuration = 60; // seconds

    private Resources mResources = SignalMonitorService.getContext().getResources();

    private int mStateCode = -1;
    private int mLastReportedStateCode = -1;
    private long mCreationTime = 0;

    /**
     * Class constructor.
     */
    public AlertCriteria(int code) {

        mStateCode = code;
        mLastReportedStateCode = code;

        mCreationTime = System.nanoTime();

        readSharedPref();
    }

    public int getStateCode() {

        return mStateCode;
    }

    public void setStateCode(int code) {

        mStateCode = code;
    }

    public int getLastReportedStateCode() {

        return mLastReportedStateCode;
    }

    public void setLastReportedStateCode(int code) {

        mLastReportedStateCode = code;
    }

    public void setTimeStamp() {

        mCreationTime = System.nanoTime(); // current time
    }

    private void readSharedPref() {

        String key = MyApp.getRes().getString(R.string.sharedPrefKey_persistence_time);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        String prefValue = prefs.getString(key, "NA");

        // If a saved value exists, use it
        if (!(prefValue.equals("NA"))) {

            persistenceDuration = Integer.parseInt(prefValue);
        }

        Log.d("testing", "readSharedPref() - Read value of " + prefValue);
    }

    public boolean isCriteriaSatisfied() {

        Log.d("testing", "AlertCriteria - using " + Integer.toString(persistenceDuration) + " secs");

        // Notification Criteria:
        //  1. Persistence time is met
        //  2. Last reported state code is not equal to current code
        return (isPersisted() && mStateCode != mLastReportedStateCode) ? true: false;
    }

    public String getCurrentServiceStateString() {

        String message = "";

        switch (mStateCode) {
            case ServiceState.STATE_IN_SERVICE:
                message = mResources.getString(R.string.notification_content_in_service);
                break;
            case ServiceState.STATE_POWER_OFF: // TODO: do not use POWER_OFF
            case ServiceState.STATE_OUT_OF_SERVICE:
                message = mResources.getString(R.string.notification_content_out_service);
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                message = mResources.getString(R.string.notification_content_emergency);
                break;
            default:
                message = mResources.getString(R.string.notification_content_unknown);
                break;
        }
        return message;
    }

    private boolean isPersisted() {

        return (getPersistenceDuration() >= persistenceDuration) ? true: false;
    }

    /**
     * Returns the duration that this alert has persisted (in seconds).
     */
    private double getPersistenceDuration() {

        double durationSecs = 0.0;

        long elapsedTimeNano = System.nanoTime() - mCreationTime;
        durationSecs = (double)elapsedTimeNano / 1000000000.0;

        return durationSecs;
    }

    /**
     * Static method to update the persistence duration based on user settings
     */
    public static void setPersistenceDuration(int timeSec) {

        persistenceDuration = timeSec;
    }
}
