package com.konzoomer.net;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.konzoomer.Konzoomer;
import com.konzoomer.OfferDetailActivity;
import com.konzoomer.OffersActivity;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.util.Distance;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 23-11-2010
 * Time: 23:18:42
 */
public class Communicator {

    private static final String TAG = "Communicator";

    private static final String URL_SERVER = "http://m.konzoomer.com/";
    private static final String REQUEST_GET_OFFERS_WITHIN_DISTANCE_URL = "mob_offers";
    private static final String REQUEST_GET_BRANDS_URL = "mob_brands";
    private static final String REQUEST_GET_STORES_WITHIN_DISTANCE_URL = "mob_storeswd";
    private static final String REQUEST_GET_STORES_IN_VIEW_URL = "mob_storesiv";
    private static final String REQUEST_GET_STORE_DETAIL_URL = "mob_store";
    private static final String REQUEST_GET_CLOSEST_STORE_IN_CHAIN_URL = "mob_closest_storeic";

    private static final String URL_OFFER_IMAGE = "http://m.konzoomer.com/offerImage";

    private static final byte COMMUNICATION_PROTOCOL_VERSION = 1;

    private static final byte REQUEST_GET_OFFERS_WITHIN_DISTANCE = 1;
    private static final byte REQUEST_GET_BRANDS = 2;
    private static final byte REQUEST_GET_STORES_WITHIN_DISTANCE = 3;
    private static final byte REQUEST_GET_STORES_IN_VIEW = 4;
    private static final byte REQUEST_GET_STORE_DETAIL = 5;
    private static final byte REQUEST_GET_CLOSEST_STORE_IN_CHAIN = 6;

    private static final byte RESPONSE_OK = 1;
    private static final byte RESPONSE_BAD_VERSION = 2;

    private static AsyncTask<Void, Void, Void> getStoresWithinDistanceTask;

    public static void getOffersWithinDistanceAsync(final long latitudeE12, final long longitudeE12, final int radiusMeters, final OffersActivity offersActivity) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean matchingCommunicationProtocolVersion = getOffersWithinDistance(latitudeE12, longitudeE12, radiusMeters);
                getBrands();
                return matchingCommunicationProtocolVersion;
            }

            @Override
            protected void onPostExecute(Boolean matchingCommunicationProtocolVersion) {
                if (!matchingCommunicationProtocolVersion)
                    Konzoomer.showUnsupportedVersionDialog(offersActivity);

                offersActivity.updateOffersList();
            }
        }.execute();
    }

    /**
     * @param latitudeE12  client latitude multiplied with 1E12
     * @param longitudeE12 client longitude multiplied with 1E12
     * @param radiusMeters indicating area containing stores, from which the offers should be gotten
     * @return whether the communication protocol version is the same as the server's
     */
    private static boolean getOffersWithinDistance(long latitudeE12, long longitudeE12, int radiusMeters) {

        try {
            HttpClient client = getHttpClient();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
            dos.writeByte(REQUEST_GET_OFFERS_WITHIN_DISTANCE);
            dos.writeLong(latitudeE12);
            dos.writeLong(longitudeE12);
            dos.writeInt(radiusMeters);

            HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_OFFERS_WITHIN_DISTANCE_URL);
            HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
            method.setEntity(entity);

            HttpResponse resp = client.execute(method);

//            int contentLength = 0;
//            InputStream is = resp.getEntity().getContent();
//            while(is.read() != -1)
//                contentLength++;
//            Log.i(TAG, "getOffersWithinDistance.contentLength=" + contentLength);

// 29.01.2011 getOffersWithinDistance.contentLength=21637 (bytes)
// konklusion: ikke værd at zip'e

            DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

            byte responseCode = dis.readByte();
            if (responseCode == RESPONSE_OK) {
                short noOfOffers = dis.readShort();
                for (short i = 0; i < noOfOffers; i++) {
                    long id = dis.readLong();
                    byte chainID = dis.readByte();
                    String name = DataInputStream.readUTF(dis);
                    String description = DataInputStream.readUTF(dis);
                    long start = dis.readLong();
                    long end = dis.readLong();
                    Set<Long> brandIDs = new HashSet<Long>();
                    byte noOfBrands = dis.readByte();
                    for (byte j = 0; j < noOfBrands; j++)
                        brandIDs.add(dis.readLong());
                    byte category = dis.readByte();
                    short totalQuantity = dis.readShort();
                    short totalQuantityTo = dis.readShort();
                    byte totalQuantityUnit = dis.readByte();
                    short unitQuantity = dis.readShort();
                    short unitQuantityTo = dis.readShort();
                    byte unitQuantityUnit = dis.readByte();
                    byte number = dis.readByte();
                    byte type = dis.readByte();
                    int priceE2 = dis.readInt();
                    short rating = dis.readShort();

//                    Log.i(TAG, "Offer read: id=" + id + ", chainID=" + chainID + ", name=" + name +
//                            ", start=" + start + ", end=" + end + ", category=" + category);

                    String sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME + " VALUES (" +
                            id + "," + chainID + ",\"" + name + "\",\"" + description + "\"," +
                            start + "," + end + "," + category + "," +
                            totalQuantity + "," + totalQuantityTo + "," + totalQuantityUnit + "," +
                            unitQuantity + "," + unitQuantityTo + "," + unitQuantityUnit + "," +
                            number + "," + type + "," + priceE2 + "," + rating + ")";
                    Konzoomer.getDB().execSQL(sql);

                    // Replace brand relations for offer_detail
                    sql = "DELETE FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE offerID=" + id;
                    Konzoomer.getDB().execSQL(sql);
                    for (long brandID : brandIDs) {
                        sql = "INSERT INTO " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " VALUES (" +
                                id + "," + brandID + ")";
                        Konzoomer.getDB().execSQL(sql);
                    }
                }

            } else if (responseCode == RESPONSE_BAD_VERSION) {
                Log.w(TAG, "Unsupported version - please upgrade");
                return false;
            }

        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static void downloadOfferImageAsync(final long id, final OfferDetailActivity offerDetailActivity, final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                offerDetailActivity.setOfferImage(downloadOfferImage(id));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                runnable.run();
            }
        }.execute();
    }

    private static Bitmap downloadOfferImage(long id) {
        try {
            URL url = new URL(URL_OFFER_IMAGE + "?id=" + id);
            InputStream is = url.openConnection().getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int current;
            while ((current = is.read()) != -1) {
                baos.write(current);
            }

            byte[] imageBytes = baos.toByteArray();
            Bitmap offerImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            try {
                File imageFile = new File(Konzoomer.getContext().getCacheDir(), "" + id + ".jpg");
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(imageBytes);
            } catch (IOException e) {
                // Unable to write image to cache -> ignore
            }

            return offerImage;
        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void getStoreDetailAsync(final long id, final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getStoreDetail(id);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                runnable.run();
            }
        }.execute();
    }

    private static void getStoreDetail(long id) {

        try {
            HttpClient client = getHttpClient();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
            dos.writeByte(REQUEST_GET_STORE_DETAIL);
            dos.writeLong(id);

            HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_STORE_DETAIL_URL);
            HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
            method.setEntity(entity);

            HttpResponse resp = client.execute(method);
            DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

            byte responseCode = dis.readByte();
            if (responseCode == RESPONSE_OK) {
                String name = DataInputStream.readUTF(dis);
                String streetName = DataInputStream.readUTF(dis);
                String streetBuildingIdentifier = DataInputStream.readUTF(dis);
                String postCodeIdentifier = DataInputStream.readUTF(dis);
                String districtName = DataInputStream.readUTF(dis);
                String districtSubDivisionIdentifier = DataInputStream.readUTF(dis);
                String contactPerson = DataInputStream.readUTF(dis);
                String telephone = DataInputStream.readUTF(dis);
                String telefax = DataInputStream.readUTF(dis);
                String email = DataInputStream.readUTF(dis);
                String website = DataInputStream.readUTF(dis);
                int mondayOpen = dis.readInt();
                int mondayClose = dis.readInt();
                int tuesdayOpen = dis.readInt();
                int tuesdayClose = dis.readInt();
                int wednesdayOpen = dis.readInt();
                int wednesdayClose = dis.readInt();
                int thursdayOpen = dis.readInt();
                int thursdayClose = dis.readInt();
                int fridayOpen = dis.readInt();
                int fridayClose = dis.readInt();
                int saturdayOpen = dis.readInt();
                int saturdayClose = dis.readInt();
                int sundayOpen = dis.readInt();
                int sundayClose = dis.readInt();

                String sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.STORE_TABLE_NAME + " VALUES (" +
                        id + ",\"" + name + "\",\"" + streetName + "\",\"" + streetBuildingIdentifier + "\",\"" +
                        postCodeIdentifier + "\",\"" + districtName + "\",\"" + districtSubDivisionIdentifier + "\",\"" +
                        contactPerson + "\",\"" + telephone + "\",\"" + telefax + "\",\"" + email + "\",\"" + website + "\"," +
                        mondayOpen + "," + mondayClose + "," + tuesdayOpen + "," + tuesdayClose + "," +
                        wednesdayOpen + "," + wednesdayClose + "," + thursdayOpen + "," + thursdayClose + "," +
                        fridayOpen + "," + fridayClose + "," + saturdayOpen + "," + saturdayClose + "," +
                        sundayOpen + "," + sundayClose + ")";
                Konzoomer.getDB().execSQL(sql);

            } else if (responseCode == RESPONSE_BAD_VERSION) {
                Log.w(TAG, "Unsupported version - please upgrade");
            }

        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void getBrands() {
        // Get unknown brandIDs
        String sql = "SELECT DISTINCT brandID FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME +
                " WHERE brandID NOT IN (SELECT _id FROM " + KonzoomerDatabaseHelper.BRAND_TABLE_NAME + ")";
        Cursor cursor = Konzoomer.getDB().rawQuery(sql, null);
        Set<Long> unknownBrands = new HashSet<Long>();
        while (cursor.moveToNext())
            unknownBrands.add(cursor.getLong(0));
        cursor.close();

        if (unknownBrands.size() > 0)
            try {
                HttpClient client = getHttpClient();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
                dos.writeByte(REQUEST_GET_BRANDS);
                dos.writeInt(unknownBrands.size());
                for (long brandID : unknownBrands)
                    dos.writeLong(brandID);

                HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_BRANDS_URL);
                HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
                method.setEntity(entity);

                HttpResponse resp = client.execute(method);
                DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

                byte responseCode = dis.readByte();
                if (responseCode == RESPONSE_OK) {
                    int noOfBrands = dis.readInt();
                    Map<Long, String> brands = new TreeMap<Long, String>();
                    for (int i = 0; i < noOfBrands; i++)
                        brands.put(dis.readLong(), dis.readUTF());

                    SQLiteDatabase db = Konzoomer.getDB();
                    for (long brandID : brands.keySet()) {
                        sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.BRAND_TABLE_NAME + " VALUES (" +
                                brandID + ",\"" + brands.get(brandID) + "\")";
                        db.execSQL(sql);
                    }

                } else if (responseCode == RESPONSE_BAD_VERSION) {
                    Log.w(TAG, "Unsupported version - please upgrade");
                }

            } catch (UnknownHostException e) {
                // Ignore for now
            } catch (SocketException e) {
                // Ignore for now
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public static void getStoresInView(int fromLatitudeE6, int toLatitudeE6, int fromLongitudeE6, int toLongitudeE6) {

        try {
            HttpClient client = getHttpClient();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
            dos.writeByte(REQUEST_GET_STORES_IN_VIEW);
            dos.writeInt(fromLatitudeE6);
            dos.writeInt(toLatitudeE6);
            dos.writeInt(fromLongitudeE6);
            dos.writeInt(toLongitudeE6);

            HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_STORES_IN_VIEW_URL);
            HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
            method.setEntity(entity);

            HttpResponse resp = client.execute(method);
            DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

            // Clear rectangle in Database
            String sql = "DELETE FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                    " WHERE latitudeE6 >= " + fromLatitudeE6 + " AND latitudeE6 <=" + toLatitudeE6 +
                    " AND longitudeE6 >=" + fromLongitudeE6 + " AND longitudeE6 <=" + toLongitudeE6;
            Konzoomer.getDB().execSQL(sql);

            byte responseCode = dis.readByte();
            if (responseCode == RESPONSE_OK) {
                short noOfStores = dis.readShort();
                for (short i = 0; i < noOfStores; i++) {
                    long id = dis.readLong();
                    byte chainID = dis.readByte();
                    int latitudeE6 = dis.readInt();
                    int longitudeE6 = dis.readInt();

//                    Log.i(TAG, "Offer read: id=" + id + ", chainID=" + chainID + ", name=" + name +
//                            ", start=" + start + ", end=" + end + ", category=" + category);

                    // Insert/replace received stores in Database
                    sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME + " VALUES (" +
                            id + "," + chainID + "," + latitudeE6 + "," + longitudeE6 + ")";
                    Konzoomer.getDB().execSQL(sql);
                }

            } else if (responseCode == RESPONSE_BAD_VERSION) {
                Log.w(TAG, "Unsupported version - please upgrade");
            }

        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    public static void getClosestStoreInChainAsync(final byte chainID, final long latitudeE12, final long longitudeE12, final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getClosestStoreInChain(chainID, latitudeE12, longitudeE12);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                runnable.run();
            }
        }.execute();
    }

    private static void getClosestStoreInChain(byte chainID, long latitudeE12, long longitudeE12) {

        try {
            HttpClient client = getHttpClient();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
            dos.writeByte(REQUEST_GET_CLOSEST_STORE_IN_CHAIN);
            dos.writeByte(chainID);
            dos.writeLong(latitudeE12);
            dos.writeLong(longitudeE12);

            HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_CLOSEST_STORE_IN_CHAIN_URL);
            HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
            method.setEntity(entity);

            HttpResponse resp = client.execute(method);
            DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

            byte responseCode = dis.readByte();
            if (responseCode == RESPONSE_OK) {
                long id = dis.readLong();
                int latitudeE6 = dis.readInt();
                int longitudeE6 = dis.readInt();
                String name = DataInputStream.readUTF(dis);
                String streetName = DataInputStream.readUTF(dis);
                String streetBuildingIdentifier = DataInputStream.readUTF(dis);
                String postCodeIdentifier = DataInputStream.readUTF(dis);
                String districtName = DataInputStream.readUTF(dis);
                String districtSubDivisionIdentifier = DataInputStream.readUTF(dis);
                String contactPerson = DataInputStream.readUTF(dis);
                String telephone = DataInputStream.readUTF(dis);
                String telefax = DataInputStream.readUTF(dis);
                String email = DataInputStream.readUTF(dis);
                String website = DataInputStream.readUTF(dis);
                int mondayOpen = dis.readInt();
                int mondayClose = dis.readInt();
                int tuesdayOpen = dis.readInt();
                int tuesdayClose = dis.readInt();
                int wednesdayOpen = dis.readInt();
                int wednesdayClose = dis.readInt();
                int thursdayOpen = dis.readInt();
                int thursdayClose = dis.readInt();
                int fridayOpen = dis.readInt();
                int fridayClose = dis.readInt();
                int saturdayOpen = dis.readInt();
                int saturdayClose = dis.readInt();
                int sundayOpen = dis.readInt();
                int sundayClose = dis.readInt();

                // Update both storesOverlay and store
                String sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME + " VALUES (" +
                        id + "," + chainID + "," + latitudeE6 + "," + longitudeE6 + ")";
                Konzoomer.getDB().execSQL(sql);

                sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.STORE_TABLE_NAME + " VALUES (" +
                        id + ",\"" + name + "\",\"" + streetName + "\",\"" + streetBuildingIdentifier + "\",\"" +
                        postCodeIdentifier + "\",\"" + districtName + "\",\"" + districtSubDivisionIdentifier + "\",\"" +
                        contactPerson + "\",\"" + telephone + "\",\"" + telefax + "\",\"" + email + "\",\"" + website + "\"," +
                        mondayOpen + "," + mondayClose + "," + tuesdayOpen + "," + tuesdayClose + "," +
                        wednesdayOpen + "," + wednesdayClose + "," + thursdayOpen + "," + thursdayClose + "," +
                        fridayOpen + "," + fridayClose + "," + saturdayOpen + "," + saturdayClose + "," +
                        sundayOpen + "," + sundayClose + ")";
                Konzoomer.getDB().execSQL(sql);

            } else if (responseCode == RESPONSE_BAD_VERSION) {
                Log.w(TAG, "Unsupported version - please upgrade");
            }

        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void getStoresWithinDistanceAsync(final long latitudeE12, final long longitudeE12, final int radiusMeters, final Runnable runnable) {
        if (getStoresWithinDistanceTask != null)
            getStoresWithinDistanceTask.cancel(false);

        getStoresWithinDistanceTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getStoresWithinDistance(latitudeE12, longitudeE12, radiusMeters);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                runnable.run();
            }
        };
        getStoresWithinDistanceTask.execute();
    }

    private static void getStoresWithinDistance(long latitudeE12, long longitudeE12, int radiusMeters) {

        try {
            HttpClient client = getHttpClient();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(COMMUNICATION_PROTOCOL_VERSION);
            dos.writeByte(REQUEST_GET_STORES_WITHIN_DISTANCE);
            dos.writeLong(latitudeE12);
            dos.writeLong(longitudeE12);
            dos.writeInt(radiusMeters);

            HttpPost method = new HttpPost(URL_SERVER + REQUEST_GET_STORES_WITHIN_DISTANCE_URL);
            HttpEntity entity = new ByteArrayEntity(baos.toByteArray());
            method.setEntity(entity);

            HttpResponse resp = client.execute(method);
            DataInputStream dis = new DataInputStream(resp.getEntity().getContent());

            // Clear circle in Database
            Set<Long> storeIDsToBeDeleted = new HashSet<Long>();
            Distance.LatLon location = new Distance.LatLon(latitudeE12 / 1E12, longitudeE12 / 1E12);
            int fromLatitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 180.0).latitude * 1E6);
            int toLatitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 0.0).latitude * 1E6);
            int fromLongitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 270.0).longitude * 1E6);
            int toLongitudeE6 = (int) Math.round(Distance.calculateDerivedPosition(location, radiusMeters, 90.0).longitude * 1E6);

            Cursor cursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                    " WHERE latitudeE6 >= " + fromLatitudeE6 + " AND latitudeE6 <=" + toLatitudeE6 +
                    " AND longitudeE6 >=" + fromLongitudeE6 + " AND longitudeE6 <=" + toLongitudeE6, null);

            while (cursor.moveToNext()) {
                int storeLatitudeE6 = cursor.getInt(2);
                int storeLongitudeE6 = cursor.getInt(3);
                if (Distance.calculateDistance(latitudeE12, longitudeE12, storeLatitudeE6, storeLongitudeE6) <= radiusMeters)
                    storeIDsToBeDeleted.add(cursor.getLong(0));
            }
            cursor.close();

            if (storeIDsToBeDeleted.size() > 0) {
                String ids = storeIDsToBeDeleted.toString();
                String sql = "DELETE FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                        " WHERE _id IN (" + ids.substring(1, ids.length() - 1) + ")";
                Konzoomer.getDB().execSQL(sql);
            }

            byte responseCode = dis.readByte();
            if (responseCode == RESPONSE_OK) {
                short noOfStores = dis.readShort();
                for (short i = 0; i < noOfStores; i++) {
                    long id = dis.readLong();
                    byte chainID = dis.readByte();
                    int latitudeE6 = dis.readInt();
                    int longitudeE6 = dis.readInt();

                    // Insert/replace received stores in Database
                    String sql = "INSERT OR REPLACE INTO " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME +
                            " VALUES (" + id + "," + chainID + "," + latitudeE6 + "," + longitudeE6 + ")";
                    Konzoomer.getDB().execSQL(sql);
                }

            } else if (responseCode == RESPONSE_BAD_VERSION) {
                Log.w(TAG, "Unsupported version - please upgrade");
            }

        } catch (UnknownHostException e) {
            // Ignore for now
        } catch (SocketException e) {
            // Ignore for now
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
