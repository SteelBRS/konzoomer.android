package com.konzoomer;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TabHost;
import com.konzoomer.util.ChainDisplay;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 04-11-2010
 * Time: 15:12:13
 */
public class StoreActivity extends TabActivity {

//    private static final String TAG = "StoreActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.store);

        Resources res = getResources();     // Resource object to get Drawables
        TabHost tabHost = getTabHost();     // The activity TabHost
        TabHost.TabSpec spec;               // Reusable TabSpec for each tab
        Intent intent = getIntent();        // Reusable Intent for each tab

        byte chainID = intent.getByteExtra("chainID", (byte) 0);
        Drawable tabIcon = getTabIcon(chainID, res);

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent(intent).setClass(this, StoreDetailActivity.class);
        spec = tabHost.newTabSpec("storeDetail").setIndicator(ChainDisplay.getName(chainID), tabIcon).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(intent).setClass(this, OffersActivity.class);
        spec = tabHost.newTabSpec("storeOffers").setIndicator(res.getString(R.string.tab_offers), res.getDrawable(R.drawable.ic_tab_offers)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

    private Drawable getTabIcon(byte chainID, Resources res) {
        BitmapDrawable chainIcon = (BitmapDrawable) res.getDrawable(ChainDisplay.getIconID(chainID));

        int tabIconHeight;
        switch(res.getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                tabIconHeight = 24;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                tabIconHeight = 32;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                tabIconHeight = 48;
                break;
            default:
                tabIconHeight = 48;
                break;
        }

        Bitmap tabIconBitmap = Bitmap.createBitmap(chainIcon.getIntrinsicWidth(), tabIconHeight, Bitmap.Config.ARGB_8888);
        new Canvas(tabIconBitmap).drawBitmap(chainIcon.getBitmap(), 0, (tabIconHeight - chainIcon.getIntrinsicHeight())/2, null);
        return new BitmapDrawable(tabIconBitmap);
    }
}
