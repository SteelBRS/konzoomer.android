package com.konzoomer.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.konzoomer.Konzoomer;
import com.konzoomer.util.Distance;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 26-12-2010
 * Time: 15:18:33
 */
public class RadiusOverlay extends Overlay {

    public static int AREA_COLOR = 0x408080FF;

    private Context context;

    public RadiusOverlay(Context context) {
        this.context = context;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {

        SharedPreferences preferences = context.getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
        int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);

        int latitudeE6 = (int) Math.round(latitudeE12 / 1E6);
        int longitudeE6 = (int) Math.round(longitudeE12 / 1E6);
        GeoPoint myLocation = new GeoPoint(latitudeE6, longitudeE6);

        Projection projection = mapView.getProjection();
        Point myCanvasLocation = projection.toPixels(myLocation, null);

        Distance.LatLon furthestLocation = Distance.calculateDerivedPosition(new Distance.LatLon(latitudeE12 / 1E12, longitudeE12 / 1E12), radiusMeters, 90.0);
        GeoPoint furthestLocationGP = new GeoPoint((int) Math.round(furthestLocation.latitude * 1E6), (int) Math.round(furthestLocation.longitude * 1E6));
        Point furthestCanvasLocation = projection.toPixels(furthestLocationGP, null);
        float radiusPixels = furthestCanvasLocation.x - myCanvasLocation.x;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(AREA_COLOR);
        canvas.drawCircle(myCanvasLocation.x, myCanvasLocation.y, radiusPixels, paint);
    }

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        SharedPreferences preferences = context.getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
        int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);

        if (Distance.calculateDistance(latitudeE12, longitudeE12, geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6()) <= radiusMeters) {
            Konzoomer.showSettingsDialog(context);
            return true;
        } else
            return false;
    }
}
