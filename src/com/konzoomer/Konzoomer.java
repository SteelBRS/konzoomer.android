package com.konzoomer;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.location.KonzoomerLocationListener;
import com.konzoomer.map.StoresMapActivity;
import com.konzoomer.net.Communicator;
import com.konzoomer.util.ChainDisplay;
import com.konzoomer.util.Distance;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 16-11-2010
 * Time: 17:59:12
 */
public class Konzoomer extends Application {

//    private static final String TAG = "Konzoomer";

    public static final String SHARED_PREFERENCES_NAME = "konzoomerPreferences";
    public static final int DEFAULT_RADIUS_METERS = 2500;
    public static final int MINIMUM_RADIUS_METERS = 200;
    public static final DecimalFormat MORE_THAN_20KM = new DecimalFormat("00.0");
    public static final DecimalFormat MORE_THAN_2KM = new DecimalFormat("#0.00");

    private static Context applicationContext;
    private static LocationManager locationManager;
    private static KonzoomerLocationListener locationListener;
    private static SQLiteDatabase db;
    private static Set<Byte> reachableChainIDs;

    static int runCount;
    static boolean configurationChanged = false;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
        db = KonzoomerDatabaseHelper.getInstance().getWritableDatabase();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new KonzoomerLocationListener(this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        final long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        final long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
        final int radiusMeters = preferences.getInt("radiusMeters", DEFAULT_RADIUS_METERS);

        // 1. Update reachable chainIDs from local database
        updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);

        // 2. Update local database from server - update reachable chainIDs
        Communicator.getStoresWithinDistanceAsync(latitudeE12, longitudeE12, radiusMeters, new Runnable() {
            public void run() {
                updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);
            }
        });
    }

    @Override
    public void onTerminate() {
        locationManager.removeUpdates(locationListener);
        db.close();
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        configurationChanged = true;
        super.onConfigurationChanged(newConfig);
    }

    public static Context getContext() {
        return applicationContext;
    }

    public static LocationManager getLocationManager() {
        return locationManager;
    }

    public static KonzoomerLocationListener getLocationListener() {
        return locationListener;
    }

    public static SQLiteDatabase getDB() {
        return db;
    }

    public static void showSettingsDialog(final Context context) {

        Resources resources = context.getResources();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View settingsView = inflater.inflate(R.layout.settings_dialog, null);
        final TextView distance = (TextView) settingsView.findViewById(R.id.distance);
        SeekBar seekBar = (SeekBar) settingsView.findViewById(R.id.distance_seekbar);
        seekBar.setMax(500);
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        final int radiusMeters = preferences.getInt("radiusMeters", DEFAULT_RADIUS_METERS);
        int closestProgress = 0;
        int closestProgressDelta = Integer.MAX_VALUE;
        for (int i = 0; i <= 500; i++) {
            int meters = getMeters(i);
            int delta = Math.abs(radiusMeters - meters);
            if (delta < closestProgressDelta) {
                closestProgressDelta = delta;
                closestProgress = i;
            }
        }
        seekBar.setProgress(closestProgress);
        setDistance(distance, radiusMeters);

        final TextView storesWithinDistance = (TextView) settingsView.findViewById(R.id.stores_within_distance);
        final long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        final long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);

        // 1. Update stores within distance from local database
        updateStoresWithinDistance(storesWithinDistance, latitudeE12, longitudeE12, radiusMeters);

        // 2. Update local database from server - update stores within distance
        Communicator.getStoresWithinDistanceAsync(latitudeE12, longitudeE12, radiusMeters, new Runnable() {
            public void run() {
                updateStoresWithinDistance(storesWithinDistance, latitudeE12, longitudeE12, radiusMeters);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int meters = getMeters(progress);
                setDistance(distance, meters);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                final int meters = getMeters(seekBar.getProgress());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("radiusMeters", meters);
                editor.commit();

                // Update stores within distance - first locally, then via. network
                final long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
                final long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);

                // 1. Update stores within distance from local database
                updateStoresWithinDistance(storesWithinDistance, latitudeE12, longitudeE12, meters);
                updateReachableChainIDs(latitudeE12, longitudeE12, meters);

                // 2. Update local database from server - update stores within distance
                Communicator.getStoresWithinDistanceAsync(latitudeE12, longitudeE12, meters, new Runnable() {
                    public void run() {
                        updateStoresWithinDistance(storesWithinDistance, latitudeE12, longitudeE12, meters);
                        updateReachableChainIDs(latitudeE12, longitudeE12, meters);

                        if (context instanceof OffersActivity)
                            ((OffersActivity) context).updateOffersList();
                    }
                });

                if (context instanceof StoresMapActivity)
                    ((StoresMapActivity) context).getMapView().invalidate();
                else if (context instanceof OffersActivity)
                    ((OffersActivity) context).updateOffersList();
            }
        });

        AlertDialog settingsDialog = new AlertDialog.Builder(context).setTitle(resources.getString(R.string.settings))
                .setCancelable(true).setIcon(android.R.drawable.ic_menu_preferences).setPositiveButton(
                        context.getString(android.R.string.ok), null).setView(settingsView).create();

        settingsDialog.show();
    }

    private static int getMeters(int progress) {
        double unit = 1.0108744721506935488825286313915;
        return MINIMUM_RADIUS_METERS + (int) Math.round((((double) progress) / 500) * Math.pow(unit, progress + 500));
    }

    private static void setDistance(TextView distance, int meters) {
        if (meters >= 20000)
            distance.setText(MORE_THAN_20KM.format(((double) meters) / 1000) + " km");
        else if (meters >= 2000)
            distance.setText(MORE_THAN_2KM.format(((double) meters) / 1000) + " km");
        else
            distance.setText("" + meters + " m");
    }

    private static void updateStoresWithinDistance(TextView storesWithinDistance, long latitudeE12, long longitudeE12, int radiusMeters) {

        Cursor cursor = getBoundingBoxStoreOverlayCursor(latitudeE12, longitudeE12, radiusMeters);

        Map<String, Integer> storesByChain = new TreeMap<String, Integer>();
        while (cursor.moveToNext()) {
            int storeLatitudeE6 = cursor.getInt(2);
            int storeLongitudeE6 = cursor.getInt(3);
            if (Distance.calculateDistance(latitudeE12, longitudeE12, storeLatitudeE6, storeLongitudeE6) <= radiusMeters) {
                byte chainID = (byte) cursor.getInt(1);
                String chainName = ChainDisplay.getName(chainID);
                if (storesByChain.containsKey(chainName))
                    storesByChain.put(chainName, storesByChain.get(chainName) + 1);
                else
                    storesByChain.put(chainName, 1);
            }
        }
        cursor.close();

        String storesWithinDistanceText = "";
        for (String chain : storesByChain.keySet()) {
            Integer noOfStoresInChain = storesByChain.get(chain);
            storesWithinDistanceText += chain + (noOfStoresInChain > 1 ? " (x" + noOfStoresInChain + ")" : "") + ", ";
        }
        if (storesWithinDistanceText.length() > 2)
            storesWithinDistance.setText(storesWithinDistanceText.substring(0, storesWithinDistanceText.length() - 2));
    }

    public static void updateReachableChainIDs(long latitudeE12, long longitudeE12, int radiusMeters) {

        Cursor cursor = getBoundingBoxStoreOverlayCursor(latitudeE12, longitudeE12, radiusMeters);

        Set<Byte> reachableChainIDs = new TreeSet<Byte>();
        while (cursor.moveToNext()) {
            int storeLatitudeE6 = cursor.getInt(2);
            int storeLongitudeE6 = cursor.getInt(3);
            if (Distance.calculateDistance(latitudeE12, longitudeE12, storeLatitudeE6, storeLongitudeE6) <= radiusMeters)
                reachableChainIDs.add((byte) cursor.getInt(1));
        }
        cursor.close();

        Konzoomer.reachableChainIDs = reachableChainIDs;        // The switch-a-roo
    }

    private static Cursor getBoundingBoxStoreOverlayCursor(long latitudeE12, long longitudeE12, int radiusMeters) {
        Distance.LatLon location = new Distance.LatLon(latitudeE12 / 1E12, longitudeE12 / 1E12);
        int fromLatitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 180.0).latitude * 1E6);
        int toLatitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 0.0).latitude * 1E6);
        int fromLongitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 270.0).longitude * 1E6);
        int toLongitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 90.0).longitude * 1E6);

        return db.rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                " WHERE latitudeE6 >= " + fromLatitudeE6 + " AND latitudeE6 <=" + toLatitudeE6 +
                " AND longitudeE6 >=" + fromLongitudeE6 + " AND longitudeE6 <=" + toLongitudeE6, null);
    }

    public static Set<Byte> getReachableChainIDs() {
        return reachableChainIDs;
    }

    public static void showAboutDialog(Context context) {
        // Try to load the a package matching the name of our own package
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String versionInfo = pInfo.versionName;

        Resources resources = context.getResources();
        String aboutTitle = resources.getString(R.string.about_konzoomer);
        String versionString = String.format(resources.getString(R.string.version), versionInfo);
        String aboutText = resources.getString(R.string.description);

        // Set up the TextView
        final TextView message = new TextView(context);
        // We'll use a SpannableString to be able to make links clickable
        final SpannableString s = new SpannableString(aboutText);

        // Set some padding
        message.setPadding(5, 5, 5, 5);
        // Set up the final string
        message.setText(versionString + "\n\n" + s);
        // Now linkify the text
        Linkify.addLinks(message, Linkify.ALL);

        AlertDialog aboutDialog = new AlertDialog.Builder(context).setTitle(aboutTitle).setCancelable(true).
                setIcon(R.drawable.ic_launcher_konzoomer).setPositiveButton(context.getString(android.R.string.ok), null).setView(message).create();
        aboutDialog.show();
    }

    public static void showUnsupportedVersionDialog(final Context context) {
        // Try to load the a package matching the name of our own package
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String versionInfo = pInfo.versionName;

        Resources resources = context.getResources();
        String upgradeTitle = resources.getString(R.string.unsupported_version);
        String versionString = String.format(resources.getString(R.string.version), versionInfo);

        // Set up the TextView
        final TextView message = new TextView(context);

        // Set some padding
        message.setPadding(5, 5, 5, 5);
        // Set up the final string
        message.setText(versionString);
        // Now linkify the text
        Linkify.addLinks(message, Linkify.ALL);

        AlertDialog upgradeDialog = new AlertDialog.Builder(context).setTitle(upgradeTitle).setCancelable(true).
                setIcon(R.drawable.ic_launcher_konzoomer).setPositiveButton(context.getString(R.string.upgrade),
                new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=com.konzoomer"));
                        context.startActivity(marketIntent);
                    }
                }).setView(message).create();
        upgradeDialog.show();
    }

    public static void clearExpiredOffers() {
        // Find expired offers which aren't in favorites
        long now = System.currentTimeMillis();

        Set<Long> favoriteIDs = new HashSet<Long>();
        Cursor favoriteIDsCursor = db.rawQuery(
                "SELECT _id FROM " + KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME, null);
        while (favoriteIDsCursor.moveToNext())
            favoriteIDs.add(favoriteIDsCursor.getLong(0));
        favoriteIDsCursor.close();

        Set<Long> expiredOfferIDs = new HashSet<Long>();
        Cursor expiredOfferIDsCursor = db.rawQuery("SELECT _id FROM " +
                KonzoomerDatabaseHelper.OFFER_TABLE_NAME + " WHERE end < " + now, null);
        while (expiredOfferIDsCursor.moveToNext())
            expiredOfferIDs.add(expiredOfferIDsCursor.getLong(0));
        expiredOfferIDsCursor.close();

        for (Long id : favoriteIDs)
            expiredOfferIDs.remove(id);

//        Log.i(TAG, "toDelete: " + expiredOfferIDs);
        for (Long id : expiredOfferIDs) {
            // 1. Delete offer
            db.execSQL("DELETE FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME + " WHERE _id=" + id);

            // 2. Delete offer-brand relations
            db.execSQL("DELETE FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE offerID=" + id);

            // 3. Delete offer image (if exists)
            File imageFile = new File(applicationContext.getCacheDir(), "" + id + ".jpg");
            if (imageFile.exists())
                imageFile.delete();
        }
    }
}
