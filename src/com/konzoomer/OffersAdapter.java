package com.konzoomer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import com.konzoomer.db.KonzoomerDatabaseHelper;
import com.konzoomer.util.ChainDisplay;
import com.konzoomer.util.OfferDisplay;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 27-11-2010
 * Time: 17:07:17
 */
public class OffersAdapter extends ResourceCursorAdapter {

//    private static final String TAG = "OffersAdapter";

    private Activity activity;

    public OffersAdapter(Activity activity, int layout, Cursor cursor) {
        super(activity, layout, cursor);

        this.activity = activity;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int offset = cursor.getColumnCount() - KonzoomerDatabaseHelper.OFFER_TABLE_COLUMNS;

        ImageView offerChainView = (ImageView) view.findViewById(R.id.offer_chain_image);
        final byte chainID = (byte) cursor.getShort(offset + 1);
        int chainIconID = ChainDisplay.getIconID(chainID);
        offerChainView.setImageDrawable(activity.getResources().getDrawable(chainIconID).mutate());
        offerChainView.setAlpha(70);

        TextView nameView = (TextView) view.findViewById(R.id.offer_name);
        final String name = cursor.getString(offset + 2);
        nameView.setText(name);

        TextView pricePerUnit = (TextView) view.findViewById(R.id.offer_price_per_unit);
        final short totalQuantity = cursor.getShort(offset + 7);
        final short totalQuantityTo = cursor.getShort(offset + 8);
        final short totalQuantityUnit = cursor.getShort(offset + 9);
        final int priceE2 = cursor.getInt(offset + 15);
        pricePerUnit.setText(OfferDisplay.getPricePerUnitText(totalQuantity, totalQuantityTo, totalQuantityUnit, priceE2));

        TextView startEnd = (TextView) view.findViewById(R.id.offer_start_end);
        final long start = cursor.getLong(offset + 4);
        final long end = cursor.getLong(offset + 5);
        OfferDisplay.setStartEnd(startEnd, start, end, false);

        TextView numberNType = (TextView) view.findViewById(R.id.offer_number_n_type);
        final short number = cursor.getShort(offset + 13);
        final short type = cursor.getShort(offset + 14);
        numberNType.setText(OfferDisplay.getNumberNType(number, type));

        TextView price = (TextView) view.findViewById(R.id.offer_price);
        TextView centis = (TextView) view.findViewById(R.id.offer_centis);
        OfferDisplay.setPrice(price, centis, priceE2);

        // Non-displayables
        final long id = cursor.getLong(offset);
        final String description = cursor.getString(offset + 3);
        final short unitQuantity = cursor.getShort(offset + 10);
        final short unitQuantityTo = cursor.getShort(offset + 11);
        final short unitQuantityUnit = cursor.getShort(offset + 12);

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent offerDetailIntent = new Intent(activity, OfferDetailActivity.class);
                offerDetailIntent.putExtra("id", id);
                offerDetailIntent.putExtra("chainID", chainID);
                offerDetailIntent.putExtra("name", name);
                offerDetailIntent.putExtra("description", description);
                offerDetailIntent.putExtra("start", start);
                offerDetailIntent.putExtra("end", end);
                offerDetailIntent.putExtra("totalQuantity", totalQuantity);
                offerDetailIntent.putExtra("totalQuantityTo", totalQuantityTo);
                offerDetailIntent.putExtra("totalQuantityUnit", totalQuantityUnit);
                offerDetailIntent.putExtra("unitQuantity", unitQuantity);
                offerDetailIntent.putExtra("unitQuantityTo", unitQuantityTo);
                offerDetailIntent.putExtra("unitQuantityUnit", unitQuantityUnit);
                offerDetailIntent.putExtra("number", number);
                offerDetailIntent.putExtra("type", type);
                offerDetailIntent.putExtra("priceE2", priceE2);
                activity.startActivity(offerDetailIntent);
            }
        });
    }
}
