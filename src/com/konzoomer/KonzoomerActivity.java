package com.konzoomer;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import com.admob.android.ads.AdManager;
import com.admob.android.ads.InterstitialAd;
import com.admob.android.ads.InterstitialAdListener;
import com.konzoomer.map.StoresMapActivity;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 21-10-2010
 * Time: 14:16:59
 */
public class KonzoomerActivity extends TabActivity implements InterstitialAdListener {

//    private static final String TAG = "KonzoomerActivity";

    private InterstitialAd interstitialAd;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Resources res = getResources();     // Resource object to get Drawables
        TabHost tabHost = getTabHost();     // The activity TabHost
        TabHost.TabSpec spec;               // Reusable TabSpec for each tab
//        Intent intent = getIntent();        // Reusable Intent for each tab
//        logIntentFlags(intent.getFlags());

        // Create an Intent to launch an Activity for the tab (to be reused)
        Intent intent = new Intent().setClass(this, FavoritesActivity.class);
        spec = tabHost.newTabSpec("favorites").setIndicator(res.getString(R.string.tab_favorites), res.getDrawable(R.drawable.ic_tab_favorites)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, OffersActivity.class);
        spec = tabHost.newTabSpec("offers").setIndicator(res.getString(R.string.tab_offers), res.getDrawable(R.drawable.ic_tab_offers)).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, StoresMapActivity.class);
        spec = tabHost.newTabSpec("map").setIndicator(res.getString(R.string.tab_map), res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(1);

        // Ignore configuration changes
        if (Konzoomer.configurationChanged)
            Konzoomer.configurationChanged = false;
        else {
            SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            Konzoomer.runCount = preferences.getInt("runCount", 0) + 1;
//            Log.i(TAG, "runCount: " + Konzoomer.runCount);

            if (Konzoomer.runCount >= 5) {
                Konzoomer.runCount = 0;

                AdManager.setTestDevices(new String[]{
                        AdManager.TEST_EMULATOR,                // Android emulator
//                        "E77194A4D5AD7A04D7122A6D6EE3A88F"      // My HTC Legend
                });

                // interstitialAd is an instance variable of the activity
                interstitialAd = new InterstitialAd(InterstitialAd.Event.APP_START, this);
                interstitialAd.requestAd(this);
            }
            preferences.edit().putInt("runCount", Konzoomer.runCount).commit();
        }
//        Log.v(TAG, "JVM default locale: " + Locale.getDefault());
    }
/*
    private void logIntentFlags(int intentFlags) {
        if ((intentFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) > 0)
            Log.i(TAG, "FLAG_GRANT_READ_URI_PERMISSION");
        if ((intentFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) > 0)
            Log.i(TAG, "FLAG_GRANT_WRITE_URI_PERMISSION");
        if ((intentFlags & Intent.FLAG_DEBUG_LOG_RESOLUTION) > 0)
            Log.i(TAG, "FLAG_DEBUG_LOG_RESOLUTION");
        if ((intentFlags & Intent.FLAG_FROM_BACKGROUND) > 0)
            Log.i(TAG, "FLAG_FROM_BACKGROUND");
        if ((intentFlags & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_BROUGHT_TO_FRONT");
        if ((intentFlags & Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
        if ((intentFlags & Intent.FLAG_ACTIVITY_CLEAR_TOP) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_CLEAR_TOP");
        if ((intentFlags & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
        if ((intentFlags & Intent.FLAG_ACTIVITY_FORWARD_RESULT) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_FORWARD_RESULT");
        if ((intentFlags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
        if ((intentFlags & Intent.FLAG_ACTIVITY_MULTIPLE_TASK) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_MULTIPLE_TASK");
        if ((intentFlags & Intent.FLAG_ACTIVITY_NEW_TASK) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_NEW_TASK");
        if ((intentFlags & Intent.FLAG_ACTIVITY_NO_HISTORY) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_NO_HISTORY");
        if ((intentFlags & Intent.FLAG_ACTIVITY_NO_USER_ACTION) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_NO_USER_ACTION");
        if ((intentFlags & Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_PREVIOUS_IS_TOP");
        if ((intentFlags & Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
        if ((intentFlags & Intent.FLAG_ACTIVITY_SINGLE_TOP) > 0)
            Log.i(TAG, "FLAG_ACTIVITY_SINGLE_TOP");
        if ((intentFlags & Intent.FLAG_RECEIVER_REGISTERED_ONLY) > 0)
            Log.i(TAG, "FLAG_RECEIVER_REGISTERED_ONLY");
    }*/

    public void onReceiveInterstitial(InterstitialAd interstitialAd) {
        // compare to saved ad in case we request several interstitials
        // from the same activity.
        if (interstitialAd == this.interstitialAd) {
            interstitialAd.show(this);
        }
    }

    public void onFailedToReceiveInterstitial(InterstitialAd interstitialAd) {
        // Do own 'fullscreen' ad
        Intent bannerIntent = new Intent(this, BannerAsFullscreenAdvertActivity.class);
        startActivity(bannerIntent);
    }

    // InterstitialAd uses an activity to show its content and returns a boolean as part of
    // the result intent to indicate the dismissal of the interstitial.

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getBooleanExtra(InterstitialAd.ADMOB_INTENT_BOOLEAN, false)) {
            // add your code here to continue with application loading
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // de-register the listener so we don't keep around the Activity for longer
    // than necessary.

    protected void onDestroy() {
        super.onDestroy();
        if (interstitialAd != null) {
            interstitialAd.setListener(null);
        }
    }
}
