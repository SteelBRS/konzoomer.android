package com.konzoomer.map;

import android.content.Context;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.konzoomer.util.Distance;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 01-11-2010
 * Time: 21:04:48
 * <p/>
 * Subclass MapView to catch panning events correctly
 */
public class KonzoomerMapView extends MapView {

    public interface OnPanChangeListener {
        public void onPanChange(MapView view);
    }

    private static final double DISTANCE_CHANGE_IN_METERS = 10;
    private static final long PAN_EVENT_TIMEOUT = 200;
    public static final int INITIAL_ZOOM_LEVEL = 16;

    private StoresMapActivity.StoresOverlay storesOverlay;
    private boolean userIsPanning = false;
    private GeoPoint lastMapCenter;
    private Timer panEventDelayTimer = new Timer();

    private KonzoomerMapView.OnPanChangeListener panChangeListener;

    public KonzoomerMapView(Context context, String apiKey, StoresMapActivity.StoresOverlay storesOverlay) {
        super(context, apiKey);
        this.storesOverlay = storesOverlay;
        lastMapCenter = getMapCenter();
    }

    public void setOnPanChangeListener(KonzoomerMapView.OnPanChangeListener l) {
        panChangeListener = l;
    }

    void updateDealersInView() {

        GeoPoint mapCenter = getMapCenter();

        int fromLatitudeE6 = Math.round(mapCenter.getLatitudeE6() - getLatitudeSpan() / 2);
        int toLatitudeE6 = Math.round(mapCenter.getLatitudeE6() + getLatitudeSpan() / 2);
        int fromLongitudeE6 = Math.round(mapCenter.getLongitudeE6() - getLongitudeSpan() / 2);
        int toLongitudeE6 = Math.round(mapCenter.getLongitudeE6() + getLongitudeSpan() / 2);

        storesOverlay.updateView(fromLatitudeE6, toLatitudeE6, fromLongitudeE6, toLongitudeE6);
    }

    void startPanningTimerTask() {
        lastMapCenter = getMapCenter();
        panEventDelayTimer.cancel();
        panEventDelayTimer = new Timer();
        panEventDelayTimer.schedule(new PanningTimerTask(), PAN_EVENT_TIMEOUT);
    }

    public boolean onTouchEvent(MotionEvent event) {

        int actionId = event.getAction();

        if (actionId == MotionEvent.ACTION_UP && userIsPanning) {

            userIsPanning = false;
            if (Distance.calculateDistance(lastMapCenter, getMapCenter()) > DISTANCE_CHANGE_IN_METERS) {

                // Map may still be scrolling after finger is lifted - check movement in PAN_EVENT_TIMEOUT milliseconds
                startPanningTimerTask();
            }

        } else if (actionId == MotionEvent.ACTION_MOVE) {
            userIsPanning = true;
        }

        try {
            return super.onTouchEvent(event);
        } catch (NullPointerException e) {
            return true;    // Event was handled as good as possible
        }
    }

    private class PanningTimerTask extends TimerTask {

        @Override
        public void run() {
            if (Distance.calculateDistance(lastMapCenter, getMapCenter()) > DISTANCE_CHANGE_IN_METERS) {
                startPanningTimerTask();

            } else {
                panChangeListener.onPanChange(KonzoomerMapView.this);
                lastMapCenter = getMapCenter();
            }
        }
    }
}
