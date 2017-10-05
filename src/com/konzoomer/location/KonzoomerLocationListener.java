package com.konzoomer.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.konzoomer.Konzoomer;
import com.konzoomer.map.StoresMapActivity;
import com.konzoomer.net.Communicator;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 28-12-2010
 * Time: 11:42:01
 */
public class KonzoomerLocationListener implements LocationListener {

    private static final String TAG = "KonzoomerLocationListener";

    private static final float LOCATION_DISTANCE_METERS_BEFORE_ANIMATE_TO = 25;
    private static final float LOCATION_DISTANCE_METERS_BEFORE_UPDATE_REACHABLE_CHAIN_IDS = 50;
    private static final long TEN_MINUTES = 10 * 60 * 1000;

    private Location lastLocation = new Location(Konzoomer.SHARED_PREFERENCES_NAME);
    private Context context;
    private SharedPreferences preferences;

    public KonzoomerLocationListener(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        lastLocation.setLatitude(preferences.getLong("lastLocation.latitudeE12", 0) / 1E12);
        lastLocation.setLongitude(preferences.getLong("lastLocation.longitudeE12", 0) / 1E12);
        lastLocation.setProvider(preferences.getString("lastLocation.provider", Konzoomer.SHARED_PREFERENCES_NAME));
        lastLocation.setTime(preferences.getLong("lastLocation.time", 0));
    }

    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {

            if (lastLocation.getProvider().equals(LocationManager.GPS_PROVIDER) &&
                    lastLocation.getTime() > System.currentTimeMillis() - TEN_MINUTES) {
                // ignore
            } else {
                // check distance from lastLocation
                if (location.distanceTo(lastLocation) > LOCATION_DISTANCE_METERS_BEFORE_UPDATE_REACHABLE_CHAIN_IDS) {

                    final int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);
                    SharedPreferences.Editor editor = preferences.edit();
                    final long latitudeE12 = Math.round(location.getLatitude() * 1E12);
                    final long longitudeE12 = Math.round(location.getLongitude() * 1E12);
                    editor.putLong("lastLocation.latitudeE12", latitudeE12);
                    editor.putLong("lastLocation.longitudeE12", longitudeE12);
                    editor.putString("lastLocation.provider", location.getProvider());
                    editor.putLong("lastLocation.time", location.getTime());
                    editor.commit();
                    // 1. Update reachable chainIDs from local database
                    Konzoomer.updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);

                    // 2. Update local database from server - update reachable chainIDs
                    Communicator.getStoresWithinDistanceAsync(latitudeE12, longitudeE12, radiusMeters, new Runnable() {
                        public void run() {
                            Konzoomer.updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);
                        }
                    });

                    lastLocation = location;
                }
            }

        } else if (LocationManager.GPS_PROVIDER.equals(provider)) {

            final int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);
            SharedPreferences.Editor editor = preferences.edit();
            final long latitudeE12 = Math.round(location.getLatitude() * 1E12);
            final long longitudeE12 = Math.round(location.getLongitude() * 1E12);
            editor.putLong("lastLocation.latitudeE12", latitudeE12);
            editor.putLong("lastLocation.longitudeE12", longitudeE12);
            editor.putString("lastLocation.provider", location.getProvider());
            editor.putLong("lastLocation.time", location.getTime());
            editor.commit();
            // 1. Update reachable chainIDs from local database
            Konzoomer.updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);

            // 2. Update local database from server - update reachable chainIDs
            Communicator.getStoresWithinDistanceAsync(latitudeE12, longitudeE12, radiusMeters, new Runnable() {
                public void run() {
                    Konzoomer.updateReachableChainIDs(latitudeE12, longitudeE12, radiusMeters);
                }
            });

            if (context instanceof StoresMapActivity) {
                // locationManager.getLastKnownLocation is poorly implemented on HTC, do own implementation
                if (location.distanceTo(lastLocation) > LOCATION_DISTANCE_METERS_BEFORE_ANIMATE_TO) {
                    StoresMapActivity storesMapActivity = (StoresMapActivity) context;
                    storesMapActivity.animateMapTo(location);
                }
            }

            lastLocation = location;

        } else
            Log.w(TAG, "Unknown location provider: " + provider);
    }

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    public void onProviderEnabled(String s) {
    }

    public void onProviderDisabled(String s) {
    }
}

