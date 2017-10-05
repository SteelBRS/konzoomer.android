package com.konzoomer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.net.Communicator;
import com.konzoomer.util.Distance;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 14-12-2010
 * Time: 13:57:34
 */
public class StoreDetailActivity extends Activity {

//    private static final String TAG = "StoreDetailActivity";

    public static final long STORE_ID_CLOSEST_IN_CHAIN = -1;

    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");

    private long id;
    private String telephone;
    private String email;
    private String website;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.store_detail);

        SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        final long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
        final long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);

        Intent intent = getIntent();
        id = intent.getLongExtra("id", 0);
        final byte chainID = intent.getByteExtra("chainID", (byte) 0);
        int latitudeE6 = intent.getIntExtra("latitudeE6", 0);
        int longitudeE6 = intent.getIntExtra("longitudeE6", 0);

        if (id == STORE_ID_CLOSEST_IN_CHAIN) {
            // 1. Populate from local database
            long foundStoreID = findClosestStoreInChain(chainID, latitudeE12, longitudeE12);
            if (foundStoreID != STORE_ID_CLOSEST_IN_CHAIN)
                populateView(foundStoreID);

            // 2. Update local database from server - populate
            Communicator.getClosestStoreInChainAsync(chainID, latitudeE12, longitudeE12, new Runnable() {
                public void run() {
                    populateView(findClosestStoreInChain(chainID, latitudeE12, longitudeE12));
                }
            });

        } else {
            // 1. Populate from local database
            populateView(id);

            // 2. Update local database from server - populate
            Communicator.getStoreDetailAsync(id, new Runnable() {
                public void run() {
                    populateView(id);
                }
            });
        }
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

    /**
     * Finds the id of the closest store in the chain locally
     *
     * @param chainID id of the chain
     * @param latitudeE12 client latitude multiplied with 1E12
     * @param longitudeE12 client longitude multiplied with 1E12
     * @return id of the closest store, or STORE_ID_CLOSEST_IN_CHAIN if no stores in the chain are found locally
     */
    private long findClosestStoreInChain(byte chainID, long latitudeE12, long longitudeE12) {

        long storeID = STORE_ID_CLOSEST_IN_CHAIN;
        double distanceToStore = Double.MAX_VALUE;
        Cursor cursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_OVERLAY_TABLE_NAME + " WHERE chainID=" + chainID, null);
        while (cursor.moveToNext()) {
            int latitudeE6 = cursor.getInt(2);
            int longitudeE6 = cursor.getInt(3);
            if (Distance.calculateDistance(latitudeE12, longitudeE12, latitudeE6, longitudeE6) < distanceToStore) {
                storeID = cursor.getLong(0);
                distanceToStore = Distance.calculateDistance(latitudeE12, longitudeE12, latitudeE6, longitudeE6);
            }
        }
        cursor.close();

        return storeID;
    }

    private void populateView(final long id) {
        String name = "";
        String streetName = "";
        String streetBuildingIdentifier = "";
        String postCodeIdentifier = "";
        String districtName = "";
        String districtSubDivisionIdentifier = "";
        String contactPerson = "";
        telephone = "";
        String telefax = "";
        email = "";
        website = "";
        int mondayOpen = -1;
        int mondayClose = -1;
        int tuesdayOpen = -1;
        int tuesdayClose = -1;
        int wednesdayOpen = -1;
        int wednesdayClose = -1;
        int thursdayOpen = -1;
        int thursdayClose = -1;
        int fridayOpen = -1;
        int fridayClose = -1;
        int saturdayOpen = -1;
        int saturdayClose = -1;
        int sundayOpen = -1;
        int sundayClose = -1;

        Cursor cursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.STORE_TABLE_NAME + " WHERE _id=" + id, null);
        if (cursor.moveToNext()) {
            name = cursor.getString(1);
            streetName = cursor.getString(2);
            streetBuildingIdentifier = cursor.getString(3);
            postCodeIdentifier = cursor.getString(4);
            districtName = cursor.getString(5);
            districtSubDivisionIdentifier = cursor.getString(6);
            contactPerson = cursor.getString(7);
            telephone = cursor.getString(8);
            telefax = cursor.getString(9);
            email = cursor.getString(10);
            website = cursor.getString(11);
            mondayOpen = cursor.getInt(12);
            mondayClose = cursor.getInt(13);
            tuesdayOpen = cursor.getInt(14);
            tuesdayClose = cursor.getInt(15);
            wednesdayOpen = cursor.getInt(16);
            wednesdayClose = cursor.getInt(17);
            thursdayOpen = cursor.getInt(18);
            thursdayClose = cursor.getInt(19);
            fridayOpen = cursor.getInt(20);
            fridayClose = cursor.getInt(21);
            saturdayOpen = cursor.getInt(22);
            saturdayClose = cursor.getInt(23);
            sundayOpen = cursor.getInt(24);
            sundayClose = cursor.getInt(25);
        }
        cursor.close();

        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(name);
        TextView addressLine1View = (TextView) findViewById(R.id.addressLine1);
        addressLine1View.setText(streetName + " " + streetBuildingIdentifier + ("".equals(districtSubDivisionIdentifier) ? "" : ", " + districtSubDivisionIdentifier));
        TextView addressLine2View = (TextView) findViewById(R.id.addressLine2);
        addressLine2View.setText(postCodeIdentifier + " " + districtName);
        Button navigationButton = (Button) findViewById(R.id.navigation);
        navigationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
                long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
                int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);
                Intent navigateToIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "geo:0,0?q=http://m.konzoomer.com/kml?id=" + id +
                                "%26latitudeE12=" + latitudeE12 + "%26longitudeE12=" + longitudeE12 + "%26radiusMeters=" + radiusMeters));
                startActivity(navigateToIntent);
            }
        });

        TextView contactPersonLabel = (TextView) findViewById(R.id.contactPersonLabel);
        if (!"".equals(contactPerson)) {
            TextView contactPersonView = (TextView) findViewById(R.id.contactPerson);
            contactPersonView.setText(contactPerson);
            contactPersonLabel.setVisibility(View.VISIBLE);
            contactPersonView.setVisibility(View.VISIBLE);
        }

        TextView telephoneLabel = (TextView) findViewById(R.id.telephoneLabel);
        if (!"".equals(telephone)) {
            Button telephoneView = (Button) findViewById(R.id.telephone);
            telephoneView.setText(telephone);
            telephoneView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + telephone));
                    startActivity(intent);
                }
            });
            telephoneLabel.setVisibility(View.VISIBLE);
            telephoneView.setVisibility(View.VISIBLE);
        }

        TextView telefaxLabel = (TextView) findViewById(R.id.telefaxLabel);
        if (!"".equals(telefax)) {
            TextView telefaxView = (TextView) findViewById(R.id.telefax);
            telefaxView.setText(telefax);
            telefaxLabel.setVisibility(View.VISIBLE);
            telefaxView.setVisibility(View.VISIBLE);
        }

        TextView emailLabel = (TextView) findViewById(R.id.emailLabel);
        if (!"".equals(email)) {
            Button emailView = (Button) findViewById(R.id.email);
            emailView.setText(email);
            emailView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    // TODO: Starts my Gmail account (vesterager@gmail.com) even though my default is vesterager@tvi.dk
//                    startActivity(intent);
                    startActivity(Intent.createChooser(intent, "Send mail..."));

                }
            });
            emailLabel.setVisibility(View.VISIBLE);
            emailView.setVisibility(View.VISIBLE);
        }

        TextView websiteLabel = (TextView) findViewById(R.id.websiteLabel);
        if (!"".equals(website)) {
            Button websiteView = (Button) findViewById(R.id.website);
            websiteView.setText(website);
            websiteView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(website));
                    startActivity(intent);
                }
            });
            websiteLabel.setVisibility(View.VISIBLE);
            websiteView.setVisibility(View.VISIBLE);
        }

        Resources resources = getResources();

        TextView mondayView = (TextView) findViewById(R.id.monday);
        mondayView.setText(getOpeningHours(mondayOpen, mondayClose, resources));
        TextView tuesdayView = (TextView) findViewById(R.id.tuesday);
        tuesdayView.setText(getOpeningHours(tuesdayOpen, tuesdayClose, resources));
        TextView wednesdayView = (TextView) findViewById(R.id.wednesday);
        wednesdayView.setText(getOpeningHours(wednesdayOpen, wednesdayClose, resources));
        TextView thursdayView = (TextView) findViewById(R.id.thursday);
        thursdayView.setText(getOpeningHours(thursdayOpen, thursdayClose, resources));
        TextView fridayView = (TextView) findViewById(R.id.friday);
        fridayView.setText(getOpeningHours(fridayOpen, fridayClose, resources));
        TextView saturdayView = (TextView) findViewById(R.id.saturday);
        saturdayView.setText(getOpeningHours(saturdayOpen, saturdayClose, resources));
        TextView sundayView = (TextView) findViewById(R.id.sunday);
        sundayView.setText(getOpeningHours(sundayOpen, sundayClose, resources));

        // Bold font current day
        TextView label;
        TextView hours;
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                label = (TextView) findViewById(R.id.mondayLabel);
                hours = (TextView) findViewById(R.id.monday);
                break;
            case Calendar.TUESDAY:
                label = (TextView) findViewById(R.id.tuesdayLabel);
                hours = (TextView) findViewById(R.id.tuesday);
                break;
            case Calendar.WEDNESDAY:
                label = (TextView) findViewById(R.id.wednesdayLabel);
                hours = (TextView) findViewById(R.id.wednesday);
                break;
            case Calendar.THURSDAY:
                label = (TextView) findViewById(R.id.thursdayLabel);
                hours = (TextView) findViewById(R.id.thursday);
                break;
            case Calendar.FRIDAY:
                label = (TextView) findViewById(R.id.fridayLabel);
                hours = (TextView) findViewById(R.id.friday);
                break;
            case Calendar.SATURDAY:
                label = (TextView) findViewById(R.id.saturdayLabel);
                hours = (TextView) findViewById(R.id.saturday);
                break;
            case Calendar.SUNDAY:
                label = (TextView) findViewById(R.id.sundayLabel);
                hours = (TextView) findViewById(R.id.sunday);
                break;
            default:
                throw new RuntimeException("Strange DAY_OF_WEEK: " + dayOfWeek);
        }
        label.setTypeface(null, Typeface.BOLD);
        hours.setTypeface(null, Typeface.BOLD);
    }

    private String getOpeningHours(int mondayOpen, int mondayClose, Resources resources) {
        if (mondayOpen != -1 && mondayClose != -1) {
            Calendar open = getResetCalendar();
            Calendar close = getResetCalendar();
            open.add(Calendar.MILLISECOND, mondayOpen);
            close.add(Calendar.MILLISECOND, mondayClose);
            return HOUR_FORMAT.format(open.getTime()) + " - " + HOUR_FORMAT.format(close.getTime());
        } else
            return resources.getString(R.string.closed);
    }

    private Calendar getResetCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar;
    }
}
