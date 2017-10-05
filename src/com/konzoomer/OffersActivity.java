package com.konzoomer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.net.Communicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 28-10-2010
 * Time: 12:55:26
 */
public class OffersActivity extends Activity {

//    private static final String TAG = "OffersActivity";

    private static final long THIRTY_MINUTES = 30 * 60 * 1000;

    private final Category[] CATEGORIES = new Category[]{
            new Category((byte) 0, (byte) 0, R.string.cat_all, R.drawable.ic_cat_empty),

            new Category((byte) 1, (byte) 0, R.string.cat_bakery, R.drawable.ic_cat_empty),
            new Category((byte) 2, (byte) 1, R.string.cat_rye_bread, R.drawable.ic_cat_empty),
            new Category((byte) 3, (byte) 1, R.string.cat_bread_n_buns, R.drawable.ic_cat_empty),
            new Category((byte) 4, (byte) 1, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 5, (byte) 0, R.string.cat_dairy, R.drawable.ic_cat_empty),
            new Category((byte) 6, (byte) 5, R.string.cat_milk_n_yoghurt, R.drawable.ic_cat_empty),
            new Category((byte) 7, (byte) 5, R.string.cat_cheese, R.drawable.ic_cat_empty),
            new Category((byte) 8, (byte) 5, R.string.cat_eggs, R.drawable.ic_cat_empty),
            new Category((byte) 9, (byte) 5, R.string.cat_butter_n_fat, R.drawable.ic_cat_empty),
            new Category((byte) 10, (byte) 5, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 11, (byte) 0, R.string.cat_meat_fish_n_poultry, R.drawable.ic_cat_empty),
            new Category((byte) 12, (byte) 11, R.string.cat_beef, R.drawable.ic_cat_empty),
            new Category((byte) 13, (byte) 11, R.string.cat_pork, R.drawable.ic_cat_empty),
            new Category((byte) 14, (byte) 11, R.string.cat_poultry, R.drawable.ic_cat_empty),
            new Category((byte) 15, (byte) 11, R.string.cat_sausages, R.drawable.ic_cat_empty),
            new Category((byte) 16, (byte) 11, R.string.cat_fish_n_seafood, R.drawable.ic_cat_empty),
            new Category((byte) 17, (byte) 11, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 18, (byte) 0, R.string.cat_fruits_n_vegetables, R.drawable.ic_cat_empty),
            new Category((byte) 19, (byte) 18, R.string.cat_fruits, R.drawable.ic_cat_empty),
            new Category((byte) 20, (byte) 18, R.string.cat_vegetables, R.drawable.ic_cat_empty),
            new Category((byte) 21, (byte) 18, R.string.cat_dried_fruit_n_nuts, R.drawable.ic_cat_empty),
            new Category((byte) 22, (byte) 18, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 23, (byte) 0, R.string.cat_cooked_meats_n_salads, R.drawable.ic_cat_empty),
            new Category((byte) 24, (byte) 23, R.string.cat_salads, R.drawable.ic_cat_empty),
            new Category((byte) 25, (byte) 23, R.string.cat_cooked_meats_sliced, R.drawable.ic_cat_empty),
            new Category((byte) 26, (byte) 23, R.string.cat_cooked_meats_whole_pieces, R.drawable.ic_cat_empty),
            new Category((byte) 27, (byte) 23, R.string.cat_pate, R.drawable.ic_cat_empty),
            new Category((byte) 28, (byte) 23, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 29, (byte) 0, R.string.cat_frozen, R.drawable.ic_cat_empty),
            new Category((byte) 30, (byte) 29, R.string.cat_bread, R.drawable.ic_cat_empty),
            new Category((byte) 31, (byte) 29, R.string.cat_meals_n_pizza, R.drawable.ic_cat_empty),
            new Category((byte) 32, (byte) 29, R.string.cat_vegetables_n_french_fries, R.drawable.ic_cat_empty),
            new Category((byte) 33, (byte) 29, R.string.cat_meat_fish_n_poultry, R.drawable.ic_cat_empty),
            new Category((byte) 34, (byte) 29, R.string.cat_soups, R.drawable.ic_cat_empty),
            new Category((byte) 35, (byte) 29, R.string.cat_ice_cream_n_desserts, R.drawable.ic_cat_empty),
            new Category((byte) 36, (byte) 29, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 37, (byte) 0, R.string.cat_processed_food, R.drawable.ic_cat_empty),
            new Category((byte) 38, (byte) 37, R.string.cat_tinned_food, R.drawable.ic_cat_empty),
            new Category((byte) 39, (byte) 37, R.string.cat_condiments_n_sauces, R.drawable.ic_cat_empty),
            new Category((byte) 40, (byte) 37, R.string.cat_sugar_n_flour, R.drawable.ic_cat_empty),
            new Category((byte) 41, (byte) 37, R.string.cat_cereals, R.drawable.ic_cat_empty),
            new Category((byte) 42, (byte) 37, R.string.cat_coffee_tea_n_cocoa, R.drawable.ic_cat_empty),
            new Category((byte) 43, (byte) 37, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 44, (byte) 0, R.string.cat_beverages, R.drawable.ic_cat_empty),
            new Category((byte) 45, (byte) 44, R.string.cat_juice, R.drawable.ic_cat_empty),
            new Category((byte) 46, (byte) 44, R.string.cat_water, R.drawable.ic_cat_empty),
            new Category((byte) 47, (byte) 44, R.string.cat_soft_drinks, R.drawable.ic_cat_empty),
            new Category((byte) 48, (byte) 44, R.string.cat_beer, R.drawable.ic_cat_empty),
            new Category((byte) 49, (byte) 44, R.string.cat_wine, R.drawable.ic_cat_empty),
            new Category((byte) 50, (byte) 44, R.string.cat_spirits, R.drawable.ic_cat_empty),
            new Category((byte) 51, (byte) 44, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 52, (byte) 0, R.string.cat_candy_n_snacks, R.drawable.ic_cat_empty),
            new Category((byte) 53, (byte) 52, R.string.cat_candy, R.drawable.ic_cat_empty),
            new Category((byte) 54, (byte) 52, R.string.cat_snacks_n_dip, R.drawable.ic_cat_empty),

            new Category((byte) 55, (byte) 0, R.string.cat_health_n_beauty, R.drawable.ic_cat_empty),
            new Category((byte) 56, (byte) 55, R.string.cat_deodorants_n_roll_on, R.drawable.ic_cat_empty),
            new Category((byte) 57, (byte) 55, R.string.cat_shaving, R.drawable.ic_cat_empty),
            new Category((byte) 58, (byte) 55, R.string.cat_haircare, R.drawable.ic_cat_empty),
            new Category((byte) 59, (byte) 55, R.string.cat_skincare, R.drawable.ic_cat_empty),
            new Category((byte) 60, (byte) 55, R.string.cat_oral_care, R.drawable.ic_cat_empty),
            new Category((byte) 61, (byte) 55, R.string.cat_other, R.drawable.ic_cat_empty),

            new Category((byte) 62, (byte) 0, R.string.cat_household, R.drawable.ic_cat_empty),
            new Category((byte) 63, (byte) 62, R.string.cat_laundry, R.drawable.ic_cat_empty),
            new Category((byte) 64, (byte) 62, R.string.cat_dishwashers_n_washing_up, R.drawable.ic_cat_empty),
            new Category((byte) 65, (byte) 62, R.string.cat_cleaning, R.drawable.ic_cat_empty),
            new Category((byte) 66, (byte) 62, R.string.cat_paper_n_bags, R.drawable.ic_cat_empty),
            new Category((byte) 67, (byte) 62, R.string.cat_lightbulbs_n_batteries, R.drawable.ic_cat_empty),
            new Category((byte) 68, (byte) 62, R.string.cat_other, R.drawable.ic_cat_empty)
    };

    private final Category[] TOP_CATEGORIES = new Category[]{
            CATEGORIES[0], CATEGORIES[1], CATEGORIES[5], CATEGORIES[11], CATEGORIES[18], CATEGORIES[23],
            CATEGORIES[29], CATEGORIES[37], CATEGORIES[44], CATEGORIES[52], CATEGORIES[55], CATEGORIES[62]
    };

    private static final HashMap<Byte, byte[]> subCategories = new HashMap<Byte, byte[]>();

    static {
        subCategories.put((byte) 1, new byte[]{1, 2, 3, 4});
        subCategories.put((byte) 5, new byte[]{5, 6, 7, 8, 9, 10});
        subCategories.put((byte) 11, new byte[]{11, 12, 13, 14, 15, 16, 17});
        subCategories.put((byte) 18, new byte[]{18, 19, 20, 21, 22});
        subCategories.put((byte) 23, new byte[]{23, 24, 25, 26, 27, 28});
        subCategories.put((byte) 29, new byte[]{29, 30, 31, 32, 33, 34, 35, 36});
        subCategories.put((byte) 37, new byte[]{37, 38, 39, 40, 41, 42, 43});
        subCategories.put((byte) 44, new byte[]{44, 45, 46, 47, 48, 49, 50, 51});
        subCategories.put((byte) 52, new byte[]{52, 53, 54});
        subCategories.put((byte) 55, new byte[]{55, 56, 57, 58, 59, 60, 61});
        subCategories.put((byte) 62, new byte[]{62, 63, 64, 65, 66, 67, 68});
    }

    private long lastTimeGetOffersInArea = 0;
    private Cursor offerCursor;
    private byte chainID;
    private byte category;
    private boolean categoriesSpinnerInitialized = false;
    private String searchBrandText;
    private long searchBrandID;
    private String searchText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.offers);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);       // type-to-search functionality
    }

    @Override
    protected void onStart() {
        super.onStart();

        Konzoomer.clearExpiredOffers();

        SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        category = (byte) preferences.getInt("category", 0);

        handleIntent(getIntent());

        updateCategoriesSpinner();

        if (System.currentTimeMillis() > lastTimeGetOffersInArea + THIRTY_MINUTES) {
            lastTimeGetOffersInArea = System.currentTimeMillis();
            long latitudeE12 = preferences.getLong("lastLocation.latitudeE12", 0);
            long longitudeE12 = preferences.getLong("lastLocation.longitudeE12", 0);
            int radiusMeters = preferences.getInt("radiusMeters", Konzoomer.DEFAULT_RADIUS_METERS);
            Communicator.getOffersWithinDistanceAsync(latitudeE12, longitudeE12, radiusMeters, this);
        }
    }

    private void updateCategoriesSpinner() {
        byte parentCategoryID = CATEGORIES[category].parentID;
        ArrayList<Category> categories = new ArrayList<Category>();
        int categoryIndex = 0;
        int index = 0;
        for (Category cat : CATEGORIES)
            if ((cat.id == parentCategoryID && parentCategoryID == 0) || cat.parentID == parentCategoryID) {
                categories.add(cat);
                if (cat.id == category)
                    categoryIndex = index;
                index++;
            }

        ArrayAdapter<Category> categoriesAdapter = new CategoriesAdapter(this, R.layout.category_item, R.id.category_name, categories);
        // Handle own data change notifications in bulk
        categoriesAdapter.setNotifyOnChange(false);

        CategoriesSpinner categoriesSpinner = (CategoriesSpinner) findViewById(R.id.categories_spinner);
        categoriesSpinner.setTopCategories(TOP_CATEGORIES);
        categoriesSpinner.setOffersActivity(this);
        categoriesSpinner.setAdapter(categoriesAdapter);

        if (categoriesSpinnerInitialized)
            categoriesSpinner.setSelection(categoryIndex, true);
        else {
            categoriesSpinner.setSelection(categoryIndex);
            categoriesSpinnerInitialized = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (offerCursor != null)
            offerCursor.requery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (offerCursor != null)
            offerCursor.deactivate();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (offerCursor != null)
            offerCursor.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appSearchData = new Bundle();
        appSearchData.putByte("category", category);
        startSearch(null, false, appSearchData, false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_search:
                onSearchRequested();
                return true;
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

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
/*            Log.i(TAG, "handleIntent.extras " + intent.getExtras());
            Log.i(TAG, "actionMsg " + intent.getStringExtra(SearchManager.ACTION_MSG));
            Log.i(TAG, "query " + intent.getStringExtra(SearchManager.QUERY));
            Log.i(TAG, "userQuery " + intent.getStringExtra(SearchManager.USER_QUERY));
            Log.i(TAG, "appData " + intent.getBundleExtra(SearchManager.APP_DATA));*/
            performSearch(query);
        } else {
            chainID = intent.getByteExtra("chainID", (byte) 0);
            updateOffersList();
        }
    }

    private void performSearch(String query) {
        if (isBrandName(query)) {
            category = 0;           // Revert category to ALL for search
            updateOffersList();

        } else {
            // TODO implement unitQuantity search

            // Treat search as free text search
            category = 0;           // Revert category to ALL for search
            searchText = query;
            updateOffersList();
        }
    }

    private boolean isBrandName(String query) {
        Cursor cursor = Konzoomer.getDB().rawQuery("SELECT _id FROM " + KonzoomerDatabaseHelper.BRAND_TABLE_NAME + " WHERE name=\"" + query + "\"", null);
        if (cursor.moveToNext()) {
            searchBrandText = query;
            searchBrandID = cursor.getLong(0);
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public void setCategory(byte category) {
        this.category = category;
    }

    /**
     * Searches (either free text or brand) ignore which chains are reachable
     */
    public void updateOffersList() {
        SharedPreferences preferences = getSharedPreferences(Konzoomer.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        preferences.edit().putInt("category", category).commit();

        if (offerCursor != null && !offerCursor.isClosed())
            offerCursor.close();

        Set<Byte> reachableChainIDs = Konzoomer.getReachableChainIDs();
        String reachableChainIDsString = reachableChainIDs.toString();
        reachableChainIDsString = reachableChainIDsString.substring(1, reachableChainIDsString.length() - 1);
//        Log.i(TAG, "reachableChainIDs: " + reachableChainIDsString);

        LinearLayout searchArea = (LinearLayout) findViewById(R.id.search_area);
        EditText searchEdit = (EditText) findViewById(R.id.search_edit);

        switch (category) {
            case 0:
                if (searchText != null) {
                    searchArea.setVisibility(View.VISIBLE);
                    searchEdit.setText(searchText);
                    offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                            " WHERE name LIKE '%" + searchText + "%' OR description LIKE '%" + searchText + "%'" +
                            " ORDER BY rating DESC", null);
                } else if (searchBrandID != 0) {
                    searchArea.setVisibility(View.VISIBLE);
                    searchEdit.setText(searchBrandText);
                    offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                            " WHERE _id IN (SELECT offerID FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE brandID=" + searchBrandID + ")" +
                            " ORDER BY rating DESC", null);
                } else if (chainID != 0)
                    offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                            " WHERE chainID=" + chainID + " ORDER BY rating DESC", null);
                else
                    offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                            " WHERE chainID IN (" + reachableChainIDsString + ") ORDER BY rating DESC", null);
                break;
            default:
                if (subCategories.keySet().contains(category)) {
                    StringBuffer categories = new StringBuffer(32);
                    for (byte subCategory : subCategories.get(category)) {
                        categories.append(",");
                        categories.append(subCategory);
                    }

                    if (searchText != null)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE (name LIKE '%" + searchText + "%' OR description LIKE '%" + searchText + "%')" +
                                " AND category IN (" + categories.substring(1) + ") ORDER BY rating DESC", null);
                    else if (searchBrandID != 0)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE _id IN (SELECT offerID FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE brandID=" + searchBrandID + ")" +
                                " AND category IN (" + categories.substring(1) + ") ORDER BY rating DESC", null);
                    else if (chainID != 0)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE category IN (" + categories.substring(1) + ") AND chainID=" + chainID + " ORDER BY rating DESC", null);
                    else
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE category IN (" + categories.substring(1) + ") AND chainID IN (" + reachableChainIDsString + ") ORDER BY rating DESC", null);
                } else {

                    if (searchText != null)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE (name LIKE '%" + searchText + "%' OR description LIKE '%" + searchText + "%')" +
                                " AND category=" + category + " ORDER BY rating DESC", null);
                    else if (searchBrandID != 0)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE _id IN (SELECT offerID FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE brandID=" + searchBrandID + ")" +
                                " AND category=" + category + " ORDER BY rating DESC", null);
                    else if (chainID != 0)
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE category=" + category + " AND chainID=" + chainID + " ORDER BY rating DESC", null);
                    else
                        offerCursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.OFFER_TABLE_NAME +
                                " WHERE category=" + category + " AND chainID IN (" + reachableChainIDsString + ") ORDER BY rating DESC", null);
                }
        }
        OffersAdapter adapter = new OffersAdapter(this, R.layout.offer_item, offerCursor);

        ListView listView = (ListView) findViewById(R.id.offers);
        listView.setAdapter(adapter);
    }

    private class CategoriesAdapter extends ArrayAdapter<Category> {

        private CategoriesAdapter(Context context, int resource, int textViewResourceId, List<Category> categories) {
            super(context, resource, textViewResourceId, categories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            return view.findViewById(R.id.category_name);
        }

        @Override
        public View getDropDownView(int position, View convertView, final ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            final Category category = getItem(position);
            ImageView icon = (ImageView) view.findViewById(R.id.category_icon);
            icon.setImageResource(category.iconID);
            ImageView expander = (ImageView) view.findViewById(R.id.expander);
            if (category.id != 0 && category.parentID == 0) {
                expander.setVisibility(View.VISIBLE);
                expander.setClickable(true);
                expander.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        // Change Adapter contents
                        clear();
                        for (Category subCategory : CATEGORIES)
                            if (subCategory.parentID == category.id)
                                add(subCategory);
                        notifyDataSetChanged();
                    }
                });
            } else {
                expander.setVisibility(View.GONE);
                expander.setClickable(false);
            }
            return view;
        }
    }

    class Category {

        private final byte id;
        private final byte parentID;
        private final int nameID;
        private final int iconID;

        public Category(byte id, byte parentID, int nameID, int iconID) {
            this.id = id;
            this.parentID = parentID;
            this.nameID = nameID;
            this.iconID = iconID;
        }

        public byte getID() {
            return id;
        }

        @Override
        public String toString() {
            return getString(nameID);
        }
    }
}
