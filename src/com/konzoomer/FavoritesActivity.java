package com.konzoomer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.konzoomer.db.KonzoomerDatabaseHelper;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 28-10-2010
 * Time: 12:55:10
 */
public class FavoritesActivity extends Activity {

//    private static final String TAG = "FavoritesActivity";

    private Cursor favoriteOfferCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.favorites);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);       // type-to-search functionality
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();

        ListView listView = (ListView) findViewById(R.id.favorites);
        favoriteOfferCursor = Konzoomer.getDB().rawQuery(
                "SELECT * FROM " + KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME + "," + KonzoomerDatabaseHelper.OFFER_TABLE_NAME + " WHERE " +
                        KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME + "._id=" + KonzoomerDatabaseHelper.OFFER_TABLE_NAME + "._id", null);
        OffersAdapter adapter = new OffersAdapter(this, R.layout.offer_item, favoriteOfferCursor);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        favoriteOfferCursor.requery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        favoriteOfferCursor.deactivate();
    }

    @Override
    protected void onStop() {
        super.onStop();

        favoriteOfferCursor.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
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
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        // TODO implement favorites search
    }
}
