package com.konzoomer.util;

import com.google.android.maps.GeoPoint;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 18-12-2010
 * Time: 13:16:14
 */
public class Distance {

    public static final double EARTHS_MEAN_RADIUS_METERS = 6371009.0;

    /**
     * Calculate distance between 2 GeoPoints
     *
     * @param p1 GeoPoint 1
     * @param p2 GeoPoint 2
     * 
     * @return distance between points in meters, or 0 if either GeoPoint is null
     */
    public static double calculateDistance(GeoPoint p1, GeoPoint p2) {

        double distance = 0;

        if (p1 != null && p2 != null) {
            double lat1 = p1.getLatitudeE6() / 1E6;
            double lon1 = p1.getLongitudeE6() / 1E6;
            double lat2 = p2.getLatitudeE6() / 1E6;
            double lon2 = p2.getLongitudeE6() / 1E6;
            distance = calculateDistance(lat1, lon1, lat2, lon2);
        }

        return distance;
    }

    /**
     * Calculate distance between 2 points
     *
     * @param latitudeE12 point1 latitude
     * @param longitudeE12 point1 longitude
     * @param latitudeE6 point2 latitude
     * @param longitudeE6 point2 longitude
     * @return distance between points in meters
     */
    public static double calculateDistance(long latitudeE12, long longitudeE12, int latitudeE6, int longitudeE6) {
        double lat1 = latitudeE12 / 1E12;
        double lon1 = longitudeE12 / 1E12;
        double lat2 = latitudeE6 / 1E6;
        double lon2 = longitudeE6 / 1E6;
        return calculateDistance(lat1, lon1, lat2, lon2);
    }

    /**
     * Calculate distance between 2 points
     *
     * @param lat1 point1 latitude
     * @param lon1 point1 longitude
     * @param lat2 point2 latitude
     * @param lon2 point2 longitude
     * @return distance between points in meters
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTHS_MEAN_RADIUS_METERS * c;
    }

    /**
     * Calculates the end-point from a given source at a given range (meters) and bearing (degrees).
     * This methods uses simple geometry equations to calculate the end-point.
     *
     * @param source Point of origin
     * @param range Range in meters
     * @param bearing Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static LatLon calculateDerivedPosition(LatLon source, double range, double bearing) {
        double latA = Math.toRadians(source.latitude);
        double lonA = Math.toRadians(source.longitude);
        double angularDistance = range / EARTHS_MEAN_RADIUS_METERS;
        double trueCourse = Math.toRadians(bearing);
        double lat = Math.asin(Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));
        double dLon = Math.atan2(Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA), Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));
        double lon = ((lonA + dLon + Math.PI) % (2*Math.PI)) - Math.PI;
        return new LatLon(Math.toDegrees(lat), Math.toDegrees(lon));
    }

    public static class LatLon {
        public final double latitude;
        public final double longitude;

        public LatLon(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
