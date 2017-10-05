package com.konzoomer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.domain.Units;
import com.konzoomer.net.Communicator;
import com.konzoomer.util.ChainDisplay;
import com.konzoomer.util.OfferDisplay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 25-11-2010
 * Time: 14:03:09
 */
public class OfferDetailActivity extends Activity {

//    private static final String TAG = "OfferDetailActivity";

    private Bitmap offerImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offer_detail);

        Intent intent = getIntent();
        final long id = intent.getLongExtra("id", 0L);
        final byte chainID = intent.getByteExtra("chainID", (byte) 0);
        String name = intent.getStringExtra("name");
        TextView offerName = (TextView) findViewById(R.id.offer_detail_name);
        offerName.setText(name);
        String description = intent.getStringExtra("description");
        TextView offerDescription = (TextView) findViewById(R.id.offer_detail_description);
        offerDescription.setText(description);
        long start = intent.getLongExtra("start", 0L);
        long end = intent.getLongExtra("end", 0L);
        TextView startEnd = (TextView) findViewById(R.id.offer_detail_start_end);
        OfferDisplay.setStartEnd(startEnd, start, end, true);
        short totalQuantity = intent.getShortExtra("totalQuantity", (short) 0);
        short totalQuantityTo = intent.getShortExtra("totalQuantityTo", (short) 0);
        short totalQuantityUnit = intent.getShortExtra("totalQuantityUnit", (short) 0);
        short unitQuantity = intent.getShortExtra("unitQuantity", (short) 0);
        short unitQuantityTo = intent.getShortExtra("unitQuantityTo", (short) 0);
        short unitQuantityUnit = intent.getShortExtra("unitQuantityUnit", (short) 0);
        short number = intent.getShortExtra("number", (short) 0);
        short type = intent.getShortExtra("type", (short) 0);
        int priceE2 = intent.getIntExtra("priceE2", 0);
        TextView price = (TextView) findViewById(R.id.offer_detail_price);
        TextView centis = (TextView) findViewById(R.id.offer_detail_centis);
        OfferDisplay.setPrice(price, centis, priceE2);

        TextView unitsInfo = (TextView) findViewById(R.id.offer_detail_units_info);
        if (number > 1) {
            String typeText = OfferDisplay.getTypePlural(type);
            unitsInfo.setText("" + number + " " + typeText + " á " + OfferDisplay.getQuantity(unitQuantity, unitQuantityTo, unitQuantityUnit));
        } else {
            String typeText = OfferDisplay.getTypeSingular(type);
            String totalQuantityText = OfferDisplay.getQuantity(totalQuantity, totalQuantityTo, totalQuantityUnit);
            if (totalQuantityUnit == Units.PIECE)
                unitsInfo.setText(typeText + " med " + totalQuantityText);
            else
                unitsInfo.setText(typeText + " på " + totalQuantityText);
        }

        TextView pricePerUnit = (TextView) findViewById(R.id.offer_detail_price_per_unit);
        pricePerUnit.setText(OfferDisplay.getPricePerUnitText(totalQuantity, totalQuantityTo, totalQuantityUnit, priceE2));

        Button chainButton = (Button) findViewById(R.id.offer_detail_chain);
        chainButton.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(ChainDisplay.getIconID(chainID)), null);
        chainButton.setText(ChainDisplay.getName(chainID) + " ");
        chainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent storeDetailIntent = new Intent(OfferDetailActivity.this, StoreActivity.class);
                storeDetailIntent.putExtra("id", StoreDetailActivity.STORE_ID_CLOSEST_IN_CHAIN);
                storeDetailIntent.putExtra("chainID", chainID);
                startActivity(storeDetailIntent);

            }
        });

        LinearLayout brandsView = (LinearLayout) findViewById(R.id.offer_detail_brands);
        List<Brand> brands = getBrands(id);
        if (brands.size() > 0) {
            BrandAdapter adapter = new BrandAdapter(this, R.layout.brand_item, R.id.brand_name, brands);
            Drawable listDivider = new ListView(this).getDivider();
            for (int i = 0; i < adapter.getCount(); i++) {
                brandsView.addView(adapter.getView(i, null, brandsView));
                if (i != adapter.getCount() - 1) {
                    ImageView divider = new ImageView(this);
                    divider.setBackgroundDrawable(listDivider);
                    brandsView.addView(divider);
                }
            }
        } else {
            TextView brandsLabel = (TextView) findViewById(R.id.offer_detail_brands_label);
            brandsLabel.setVisibility(View.GONE);
            brandsView.setVisibility(View.GONE);
        }

        if (offerImageExists(id))
            populateOfferImage();           // taken from cache
        else
            Communicator.downloadOfferImageAsync(id, this, new Runnable() {
                public void run() {
                    populateOfferImage();
                }
            });

        final Button backButton = (Button) findViewById(R.id.offer_detail_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        final Button favoritesButton = (Button) findViewById(R.id.offer_detail_favorites_button);
        if (isOfferInFavorites(id))
            favoritesButton.setText(R.string.button_remove_from_favorites);
        else
            favoritesButton.setText(R.string.button_add_to_favorites);

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                favoritesButtonPressed(id, favoritesButton);
            }
        });
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

    private void favoritesButtonPressed(long id, Button favoritesButton) {
        if (isOfferInFavorites(id)) {
            Konzoomer.getDB().execSQL("DELETE FROM " + KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME + " WHERE _id=" + id);
            favoritesButton.setText(R.string.button_add_to_favorites);
        } else {
            Konzoomer.getDB().execSQL("INSERT INTO " + KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME + " VALUES(" + id + ")");
            favoritesButton.setText(R.string.button_remove_from_favorites);
        }
    }

    public void setOfferImage(Bitmap offerImage) {
        this.offerImage = offerImage;
    }

    private void populateOfferImage() {
        ImageView offerImageView = (ImageView) findViewById(R.id.offer_detail_image);

        offerImageView.setImageBitmap(offerImage);
    }

    private List<Brand> getBrands(long id) {
        Map<String, Brand> brands = new TreeMap<String, Brand>();
        Cursor cursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.BRAND_TABLE_NAME +
                " WHERE _id IN (SELECT brandID FROM " + KonzoomerDatabaseHelper.OFFER_BRAND_RELATION_NAME + " WHERE offerID=" + id + ")", null);
        while (cursor.moveToNext())
            brands.put(cursor.getString(1), new Brand(cursor.getLong(0), cursor.getString(1)));
        cursor.close();
        return new ArrayList<Brand>(brands.values());
    }

    private boolean offerImageExists(long id) {
        File imageFile = new File(Konzoomer.getContext().getCacheDir(), "" + id + ".jpg");
        if (imageFile.exists()) {
            offerImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            return true;
        } else
            return false;
    }

    public boolean isOfferInFavorites(long id) {
        Cursor cursor = Konzoomer.getDB().rawQuery("SELECT * FROM " + KonzoomerDatabaseHelper.FAVORITE_OFFER_TABLE_NAME + " WHERE _id=" + id, null);
        if (cursor.moveToFirst()) {
            // favoriteOffer exists for this offer_detail
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    private class BrandAdapter extends ArrayAdapter<Brand> {

        private BrandAdapter(Context context, int resource, int textViewResourceId, List<Brand> brands) {
            super(context, resource, textViewResourceId, brands);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SEARCH, OfferProvider.CONTENT_URI, OfferDetailActivity.this, OffersActivity.class);
                    intent.putExtra(SearchManager.QUERY, getItem(position).name);
                    startActivity(intent);
                }
            });
            return view;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }
    }

    private class Brand {
        private final long id;
        private final String name;

        private Brand(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
