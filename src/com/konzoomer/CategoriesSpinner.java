package com.konzoomer;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 14-11-2010
 * Time: 19:30:52
 */
public class CategoriesSpinner extends Spinner {

    private OffersActivity.Category[] topCategories;
    private OffersActivity offersActivity;
    private ArrayAdapter<OffersActivity.Category> categoriesAdapter;

    public CategoriesSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTopCategories(OffersActivity.Category[] topCategories) {
        this.topCategories = topCategories;
    }

    public void setOffersActivity(OffersActivity offersActivity) {
        this.offersActivity = offersActivity;
    }

    @Override
    public SpinnerAdapter getAdapter() {
        return categoriesAdapter;
    }

    public void setAdapter(ArrayAdapter<OffersActivity.Category> adapter) {
        super.setAdapter(adapter);
        categoriesAdapter = adapter;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        offersActivity.setCategory(categoriesAdapter.getItem(which).getID());
        offersActivity.updateOffersList();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean toReturn = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Revert to top categories
            categoriesAdapter.clear();
            for (OffersActivity.Category category : topCategories)
                categoriesAdapter.add(category);

            categoriesAdapter.notifyDataSetChanged();
            setSelection(0, true);
        }

        return toReturn;
    }
}
