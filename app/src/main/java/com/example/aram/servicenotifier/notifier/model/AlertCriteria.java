package com.example.aram.servicenotifier.notifier.model;

import android.content.SharedPreferences;
import android.content.res.Resources;
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

    private static int persistenceDuration = 60; // seconds, default

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

    /**
     * Loads user settings upon initialization
     */
    private void readSharedPref() {

        String key = MyApp.getRes().getString(R.string.sharedPrefKey_persistence_time);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        String prefValue = prefs.getString(key, "NA");

        // If a saved value exists, use it
        if (!(prefValue.equals("NA"))) {

            persistenceDuration = Integer.parseInt(prefValue);
        }
    }

    public boolean isCriteriaSatisfied() {

        // Notification Criteria:
        //  1. Persistence time is met
        //  2. Last reported state code is not equal to current code
        //  3. State is not POWER_OFF (e.g. user activated airplane mode)
        boolean success =  (isPersisted() &&
                            mStateCode != mLastReportedStateCode &&
                            mStateCode != ServiceState.STATE_POWER_OFF);

        return success;
    }

    private boolean isPersisted() {

        return (getPersistenceTime() >= persistenceDuration) ? true: false;
    }

    /**
     * Returns the duration that this alert has persisted (in seconds).
     */
    private double getPersistenceTime() {

        double durationSecs = 0.0;

        long elapsedTimeNano = System.nanoTime() - mCreationTime;
        durationSecs = (double)elapsedTimeNano / 1000000000.0;

        return durationSecs;
    }

    /**
     * Static method to update the persistence duration based on user settings
     */
    public static int getPersistenceDuration() {

        return persistenceDuration;
    }

    /**
     * Static method to update the persistence duration based on user settings
     */
    public static void setPersistenceDuration(int timeSec) {

        persistenceDuration = timeSec;
    }

    /**
     * Returns a human-readable message describing the current service state
     */
    public String getCurrentServiceStateString() {

        String message = "";

        switch (mStateCode) {
            case ServiceState.STATE_IN_SERVICE:
                message = mResources.getString(R.string.notification_content_in_service);
                break;
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
}
