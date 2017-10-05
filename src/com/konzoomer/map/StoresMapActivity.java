package com.konzoomer.map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ZoomButtonsController;
import com.google.android.maps.*;
import com.konzoomer.Konzoomer;
import com.konzoomer.R;
import com.konzoomer.StoreActivity;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.net.Communicator;
import com.konzoomer.util.ChainDisplay;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 28-10-2010
 * Time: 12:03:01
 */
public class StoresMapActivity extends MapActivity {

//    private static final String TAG = "StoresMapActivity";

    private static final long ZOOM_EVENT_TIMEOUT = 250;

    private KonzoomerMapView mapView;
    private MapController mapController;

    private StoresOverlay storesOverlay;
    private MyLocationOverlay myLocationOverlay;
    private RadiusOverlay radiusOverlay;

    private Timer zoomEventDelayTimer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storesOverlay = new StoresOverlay();
//        mapView = new KonzoomerMapView(this, "0zr_iKVS7cb50w9pIEbk0VLeHrFpFXJrfP4qrCA", storesOverlay);     // konzoomer release key
        mapView = new KonzoomerMapView(this, "0zr_iKVS7cb5X9jnwisUo2TbkKQOxx4gEsKJHPQ", storesOverlay);     // XForce
//        mapView = new KonzoomerMapView(this, "0zr_iKVS7cb7m_qjg5k9jErSnDy6YyKsGD1NU8Q", storesOverlay);     // SteelBook-Pro
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);

        final ZoomButtonsController zoomButtonsController = mapView.getZoomButtonsController();
        zoomButtonsController.setOnZoomListener(new ZoomButtonsController.OnZoomListener() {

            public void onVisibilityChanged(boolean b) {
            }

            public void onZoom(boolean zoomIn) {

                if (zoomIn)
                    mapController.zoomIn();
                else
                    mapController.zoomOut();

                zoomEventDelayTimer.cancel();
                zoomEventDelayTimer = new Timer();
                zoomEventDelayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mapView.getZoomLevel() <= 14) {
                            mapView.post(new Runnable() {
                                public void run() {
                                    List<Overlay> overlays = mapView.getOverlays();
                                    overlays.remove(storesOverlay);

                                    mapView.invalidate();
                                }
                            });
                        } else {
                            mapView.updateDealersInView();

                            mapView.post(new Runnable() {
                                public void run() {
                                    List<Overlay> overlays = mapView.getOverlays();
                                    if (!overlays.contains(storesOverlay))
                                        overlays.add(storesOverlay);

                                    mapView.invalidate();
                                }
                            });
                        }
                    }
                }, ZOOM_EVENT_TIMEOUT);
            }
        });

        mapView.setOnPanChangeListener(new KonzoomerMapView.OnPanChangeListener() {
            public void onPanChange(MapView view) {

                if (view.getZoomLevel() > 14) {
                    mapView.updateDealersInView();

                    mapView.post(new Runnable() {
                        public void run() {
                            List<Overlay> overlays = mapView.getOverlays();
                            if (!overlays.contains(storesOverlay))
                                overlays.add(storesOverlay);

                            mapView.invalidate();
                        }
                    });
                }
            }
        });

        mapController = mapView.getController();
        mapController.setZoom(KonzoomerMapView.INITIAL_ZOOM_LEVEL);

        setContentView(mapView);

        radiusOverlay = new RadiusOverlay(this);
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        List<Overlay> overlays = mapView.getOverlays();
        overlays.add(radiusOverlay);
        overlays.add(myLocationOverlay);

        // Scroll to location and activate storesOverlay
        SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
        if(latitudeE12 != 0 && longitudeE12 != 0) {
            Location location = new Location("");
            location.setLatitude(latitudeE12 / 1E12);
            location.setLongitude(longitudeE12 / 1E12);
            animateMapTo(location);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        myLocationOverlay.disableMyLocation();
        Konzoomer.getLocationManager().removeUpdates(Konzoomer.getLocationListener());
        Konzoomer.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, Konzoomer.getLocationListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        myLocationOverlay.enableMyLocation();
        Konzoomer.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Konzoomer.getLocationListener());
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Disable search
        return keyCode == KeyEvent.KEYCODE_SEARCH || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_no_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_settings:
                Konzoomer.showSettingsDialog(this);
                return true;
            case R.id.option_about:
                Konzoomer.showAboutDialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void animateMapTo(Location location) {
        if (location != null) {
            GeoPoint geoPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
            mapController.animateTo(geoPoint, new Runnable() {
                public void run() {
                    mapView.startPanningTimerTask();
                }
            });
        }
    }

    public KonzoomerMapView getMapView() {
        return mapView;
    }

    private class Store {
        private final long id;
        private final byte chainID;
        private final int latitudeE6;
        private final int longitudeE6;

        private Store(final long id, final byte chainID, final int latitudeE6, final int longitudeE6) {
            this.id = id;
            this.chainID = chainID;
            this.latitudeE6 = latitudeE6;
            this.longitudeE6 = longitudeE6;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Store) {
                Store other = (Store) obj;
                return id == other.id && chainID == other.chainID && latitudeE6 == other.latitudeE6 && longitudeE6 == other.longitudeE6;
            } else
                return false;
        }
    }

    class StoresOverlay extends ItemizedOverlay {

        private final Map<Long, Store> storesOverlay = new HashMap<Long, Store>();

        public StoresOverlay() {
            super(null);
        }

        @Override
        protected OverlayItem createItem(int i) {
            Store store = new ArrayList<Store>(storesOverlay.values()).get(i);
            OverlayItem overlayItem = new OverlayItem(new GeoPoint(store.latitudeE6, store.longitudeE6), "Spar", "Sur");
            Resources resources = getResources();
            overlayItem.setMarker(boundCenter(resources.getDrawable(ChainDisplay.getIconID(store.chainID))));
            return overlayItem;
        }

        @Override
        public int size() {
            return storesOverlay.size();
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, false);
        }

        @Override
        protected boolean onTap(int index) {
            Store store = new ArrayList<Store>(storesOverlay.values()).get(index);
            Intent storeDetailIntent = new Intent(StoresMapActivity.this, StoreActivity.class);
            storeDetailIntent.putExtra("id", store.id);
            storeDetailIntent.putExtra("chainID", store.chainID);
            storeDetailIntent.putExtra("latitudeE6", store.latitudeE6);
            storeDetailIntent.putExtra("longitudeE6", store.longitudeE6);
            startActivity(storeDetailIntent);
            return true;
        }

        public void updateView(final int fromLatitudeE6, final int toLatitudeE6, final int fromLongitudeE6, final int toLongitudeE6) {

            // 1. Update overlay from local database
            new AsyncTask<Void, Void, Cursor>() {

                @Override
                protected Cursor doInBackground(Void... voids) {
                    return Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                            " WHERE latitudeE6 >= " + fromLatitudeE6 + " AND latitudeE6 <=" + toLatitudeE6 +
                            " AND longitudeE6 >=" + fromLongitudeE6 + " AND longitudeE6 <=" + toLongitudeE6, null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    while (cursor.moveToNext())
                        storesOverlay.put(cursor.getLong(0), new Store(cursor.getLong(0), (byte) cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));

                    cursor.close();
                    populate();
                    mapView.invalidate();
                }

            }.execute();

            // 2. Update local database from server - update overlay
            new AsyncTask<Void, Void, Cursor>() {

                @Override
                protected Cursor doInBackground(Void... voids) {
                    Communicator.getStoresInView(fromLatitudeE6, toLatitudeE6, fromLongitudeE6, toLongitudeE6);
                    return Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                            " WHERE latitudeE6 >= " + fromLatitudeE6 + " AND latitudeE6 <=" + toLatitudeE6 +
                            " AND longitudeE6 >=" + fromLongitudeE6 + " AND longitudeE6 <=" + toLongitudeE6, null);
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    // Clear rectangle in storesOverlay
                    // Insert/replace received stores in storesOverlay
                    while (cursor.moveToNext())
                        storesOverlay.put(cursor.getLong(0), new Store(cursor.getLong(0), (byte) cursor.getInt(1), cursor.getInt(2), cursor.getInt(3)));

                    cursor.close();
                    populate();
                    mapView.invalidate();
                }

            }.execute();
        }
    }
}
