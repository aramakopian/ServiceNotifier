package com.example.aram.servicenotifier.main;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.aram.servicenotifier.R;
import com.example.aram.servicenotifier.about.AboutActivity;
import com.example.aram.servicenotifier.infrastructure.MyApp;
import com.example.aram.servicenotifier.settings.SettingsActivity;
import com.example.aram.servicenotifier.view.FancyControlButton;

/**
 * Class MainActivity
 */
public class MainActivity extends ActionBarActivity implements MainView, View.OnClickListener {

    private FancyControlButton mControlButton;
    private TextView mHintMessage;
    private TextView mStateMessage;

    public static final String APP_PREFERENCES = "ServiceNotifierPreferences";
    public static final String SERVICE_ENABLED = "is_service_enabled";

    private static final long CLICK_DELAY_MS = 750; // in milliseconds
    private long mLastClickTime;

    private MainPresenter mPresenter;
    private ObjectAnimator mHintMessageAnimator;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
//            // WARNING: Only used in developer mode
//            case R.id.action_debug:
//                startActivity(new Intent(this, DebugActivity.class));
//                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Get handles to buttons
        mControlButton = (FancyControlButton) findViewById(R.id.mainView_control_button);
        mHintMessage   = (TextView) findViewById(R.id.mainView_hint_text);
        mStateMessage  = (TextView) findViewById(R.id.mainView_state_text);

        mPresenter = new MainPresenterImpl(this);
        mControlButton.setOnClickListener(this);

        // Setup animators
        mHintMessageAnimator = ObjectAnimator.ofFloat(mHintMessage, "alpha", 0.2f, 1f);
        mHintMessageAnimator.setDuration(4000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set the control button state, start looping animations
        mPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter = null;
        mHintMessageAnimator = null;
    }

    /**
     * Sets the button to the On position upon activity startup
     *
     * Called during activity onStart() sequence
     */
    @Override
    public void setButtonOn() {

        mControlButton.setStartPositionOn();
    }

    @Override
    public void playButtonAnimation() {

        mControlButton.clicked();
    }

    @Override
    public void stopButtonAnimation() {

        mControlButton.stopAnimation();
    }

    @Override
    public void setHintMessage(String message) {

        mHintMessage.setText(message);
        mHintMessageAnimator.start();
    }

    @Override
    public void setStateMessage(String message) {

        mStateMessage.setText(message);
    }

    @Override
    public void saveSessionData(boolean isRunning) {

        // Save service state to restore it in case
        // of system reboot
        SharedPreferences sharedPref = MyApp.getContext().getSharedPreferences(
                APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();

        edit.clear();
        edit.putBoolean(SERVICE_ENABLED, isRunning);
        edit.commit();
    }

    @Override
    public void onClick(View v) {

        if (allowClick()) {
            switch (v.getId()) {
                case R.id.mainView_control_button:
                    mPresenter.toggleNotificationState();
                    break;
            }
        }
    }

    /**
     * Rate limits clicks on this view
     */
    private boolean allowClick() {

        boolean allow = false;
        long now = System.currentTimeMillis();

        if (now >= mLastClickTime + CLICK_DELAY_MS) {
            mLastClickTime = now;
            allow = true;
        }
        return allow;
    }
}
