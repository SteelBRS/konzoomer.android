package com.konzoomer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.konzoomer.Konzoomer;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 23-11-2010
 * Time: 19:54:20
 */
public class KonzoomerDatabaseHelper extends SQLiteOpenHelper {

    private static KonzoomerDatabaseHelper instance;

    public static final String DATABASE_NAME = "konzoomer";
    public static final int DATABASE_VERSION = 1;

    public static final String BRAND_TABLE_NAME = "brand";
    private static final String BRAND_TABLE_CREATE = "CREATE TABLE " + BRAND_TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY, " +
            "name TEXT);";

    public static final String OFFER_TABLE_NAME = "offer";
    public static final byte OFFER_TABLE_COLUMNS = 17;
    private static final String OFFER_TABLE_CREATE = "CREATE TABLE " + OFFER_TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY, " +
            "chainID INTEGER, " +
            "name TEXT, " +
            "description TEXT, " +
            "start INTEGER, " +
            "end INTEGER, " +
            "category INTEGER, " +
            "totalQuantity INTEGER, " +
            "totalQuantityTo INTEGER, " +
            "totalQuantityUnit INTEGER, " +
            "unitQuantity INTEGER, " +
            "unitQuantityTo INTEGER, " +
            "unitQuantityUnit INTEGER, " +
            "number INTEGER, " +
            "type INTEGER, " +
            "priceE2 INTEGER, " +
            "rating INTEGER);";

    public static final String FAVORITE_OFFER_TABLE_NAME = "favoriteOffer";
    private static final String FAVORITE_OFFER_TABLE_CREATE = "CREATE TABLE " + FAVORITE_OFFER_TABLE_NAME + " (" +
            "_id INTEGER UNIQUE);";

    public static final String OFFER_BRAND_RELATION_NAME = "offerBrand";
    private static final String OFFER_BRAND_RELATION_CREATE = "CREATE TABLE " + OFFER_BRAND_RELATION_NAME + " (" +
            "offerID INTEGER, " +
            "brandID INTEGER, " +
            "UNIQUE(offerID, brandID) ON CONFLICT REPLACE);";

    public static final String STORE_OVERLAY_TABLE_NAME = "storeOverlay";
    private static final String STORE_OVERLAY_TABLE_CREATE = "CREATE TABLE " + STORE_OVERLAY_TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY, " +
            "chainID INTEGER, " +
            "latitudeE6 INTEGER, " +
            "longitudeE6 INTEGER);";

    public static final String STORE_TABLE_NAME = "store";
    private static final String STORE_TABLE_CREATE = "CREATE TABLE " + STORE_TABLE_NAME + " (" +
            "_id INTEGER PRIMARY KEY, " +
            "name TEXT, " +
            "streetName TEXT, " +
            "streetBuildingIdentifier TEXT, " +
            "postCodeIdentifier TEXT, " +
            "districtName TEXT, " +
            "districtSubDivisionIdentifier TEXT, " +
            "contactPerson TEXT, " +
            "telephone TEXT, " +
            "telefax TEXT, " +
            "email TEXT, " +
            "website TEXT, " +
            "mondayOpen INTEGER, " +
            "mondayClose INTEGER, " +
            "tuesdayOpen INTEGER, " +
            "tuesdayClose INTEGER, " +
            "wednesdayOpen INTEGER, " +
            "wednesdayClose INTEGER, " +
            "thursdayOpen INTEGER, " +
            "thursdayClose INTEGER, " +
            "fridayOpen INTEGER, " +
            "fridayClose INTEGER, " +
            "saturdayOpen INTEGER, " +
            "saturdayClose INTEGER, " +
            "sundayOpen INTEGER, " +
            "sundayClose INTEGER);";

    private KonzoomerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static KonzoomerDatabaseHelper getInstance() {
        synchronized (KonzoomerDatabaseHelper.class) {
            if (instance == null)
                instance = new KonzoomerDatabaseHelper(Konzoomer.getContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BRAND_TABLE_CREATE);
        db.execSQL(OFFER_TABLE_CREATE);
        db.execSQL(FAVORITE_OFFER_TABLE_CREATE);

        db.execSQL(STORE_OVERLAY_TABLE_CREATE);
        db.execSQL(STORE_TABLE_CREATE);

        db.execSQL(OFFER_BRAND_RELATION_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
