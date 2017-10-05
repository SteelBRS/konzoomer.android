package com.konzoomer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 15-12-2010
 * Time: 13:33:28
 */
public class StoreDetailLinearLayout extends LinearLayout {

//    private static final String TAG = "StoreDetailLinearLayout";

    public StoreDetailLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int labelWidth = 40;

        LinearLayout openingHoursLabels = (LinearLayout) findViewById(R.id.openingHoursLabels);
        TextView contactPersonLabel = (TextView) findViewById(R.id.contactPersonLabel);
        TextView telephoneLabel = (TextView) findViewById(R.id.telephoneLabel);
        TextView telefaxLabel = (TextView) findViewById(R.id.telefaxLabel);
        TextView emailLabel = (TextView) findViewById(R.id.emailLabel);
        TextView websiteLabel = (TextView) findViewById(R.id.websiteLabel);

        if (contactPersonLabel.getVisibility() == View.VISIBLE)
            labelWidth = Math.max(labelWidth, contactPersonLabel.getMeasuredWidth());
        if (telephoneLabel.getVisibility() == View.VISIBLE)
            labelWidth = Math.max(labelWidth, telephoneLabel.getMeasuredWidth());
        if (telefaxLabel.getVisibility() == View.VISIBLE)
            labelWidth = Math.max(labelWidth, telefaxLabel.getMeasuredWidth());
        if (emailLabel.getVisibility() == View.VISIBLE)
            labelWidth = Math.max(labelWidth, emailLabel.getMeasuredWidth());
        if (websiteLabel.getVisibility() == View.VISIBLE)
            labelWidth = Math.max(labelWidth, websiteLabel.getMeasuredWidth());

//        Log.i(TAG, "labelWidth=" + labelWidth);

        openingHoursLabels.setPadding(labelWidth + 2, 0, 0, 5);
        if (contactPersonLabel.getVisibility() == View.VISIBLE)
            contactPersonLabel.setWidth(labelWidth);
        if (telephoneLabel.getVisibility() == View.VISIBLE)
            telephoneLabel.setWidth(labelWidth);
        if (telefaxLabel.getVisibility() == View.VISIBLE)
            telefaxLabel.setWidth(labelWidth);
        if (emailLabel.getVisibility() == View.VISIBLE)
            emailLabel.setWidth(labelWidth);
        if (websiteLabel.getVisibility() == View.VISIBLE)
            websiteLabel.setWidth(labelWidth);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
