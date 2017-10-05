package com.konzoomer.util;

import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.TextView;
import com.konzoomer.domain.Types;
import com.konzoomer.domain.Units;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 27-11-2010
 * Time: 11:01:58
 */
public class OfferDisplay {

//    private static final String TAG = "OfferDisplay";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d. MMM");
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat LITER_FORMAT = new DecimalFormat("#,##0.0");
    private static final TimeZone TIME_ZONE_GMT_PLUS_ONE = TimeZone.getTimeZone("GMT+1");

    private static ColorStateList DEFAULT_COLOR_STATE_LIST;

    static {
        DATE_FORMAT.setTimeZone(TIME_ZONE_GMT_PLUS_ONE);
    }

    public static String getNumberNType(short number, short type) {
        String text = "";
        switch (type) {
            case Types.PIECE:
                text = number > 1 ? "" + number + " styk" : "pr. styk";
                break;
            case Types.PACKAGE:
                text = number > 1 ? "" + number + " pakker" : "pr. pakke";
                break;
            case Types.BAG:
                text = number > 1 ? "" + number + " poser" : "pr. pose";
                break;
            case Types.GLASS:
                text = number > 1 ? "" + number + " glas" : "pr. glas";
                break;
            case Types.CAN:
                text = number > 1 ? "" + number + " dåser" : "pr. dåse";
                break;
            case Types.BOTTLE:
                text = number > 1 ? "" + number + " flasker" : "pr. flaske";
                break;
            case Types.BOX:
                text = number > 1 ? "" + number + " kasser" : "pr. kasse";
                break;
            case Types.HALF_KILOGRAM:
                text = number > 1 ? "" + number + " ½ kg." : "pr. ½ kg.";
                break;
            case Types.TWO_PACK:
                text = number > 1 ? "" + number + " 2-pakker" : "pr. 2-pak";
                break;
            case Types.THREE_PACK:
                text = number > 1 ? "" + number + " 3-pakker" : "pr. 3-pak";
                break;
            case Types.FOUR_PACK:
                text = number > 1 ? "" + number + " 4-pakker" : "pr. 4-pak";
                break;
        }
        return text;
    }

    public static String getPricePerUnitText(short totalQuantity, short totalQuantityTo, short totalQuantityUnit, int priceE2) {
        String text = "";
        switch (totalQuantityUnit) {
            case Units.LITER:
                if (totalQuantityTo > 0)
                    text = "pr. liter " + AMOUNT_FORMAT.format((0.01 / totalQuantityTo) * priceE2) + " - " + AMOUNT_FORMAT.format((0.01 / totalQuantity) * priceE2);
                else
                    text = "pr. liter " + AMOUNT_FORMAT.format((0.01 / totalQuantity) * priceE2);
                break;
            case Units.CENTILITER:
                if (totalQuantityTo > 0)
                    text = "pr. liter " + AMOUNT_FORMAT.format((1.0 / totalQuantityTo) * priceE2) + " - " + AMOUNT_FORMAT.format((1.0 / totalQuantity) * priceE2);
                else
                    text = "pr. liter " + AMOUNT_FORMAT.format((1.0 / totalQuantity) * priceE2);
                break;
            case Units.MILLILITER:
                if (totalQuantityTo > 0)
                    text = "pr. liter " + AMOUNT_FORMAT.format((10.0 / totalQuantityTo) * priceE2) + " - " + AMOUNT_FORMAT.format((10.0 / totalQuantity) * priceE2);
                else
                    text = "pr. liter " + AMOUNT_FORMAT.format((10.0 / totalQuantity) * priceE2);
                break;
            case Units.GRAM:
                if (totalQuantityTo > 0)
                    text = "pr. kg. " + AMOUNT_FORMAT.format((10.0 / totalQuantityTo) * priceE2) + " - " + AMOUNT_FORMAT.format((10.0 / totalQuantity) * priceE2);
                else
                    text = "pr. kg. " + AMOUNT_FORMAT.format((10.0 / totalQuantity) * priceE2);
                break;
            case Units.PIECE:
                if (totalQuantityTo > 0)
                    text = "pr. stk. " + AMOUNT_FORMAT.format((0.01 / totalQuantityTo) * priceE2) + " - " + AMOUNT_FORMAT.format((0.01 / totalQuantity) * priceE2);
                else
                    text = "pr. stk. " + AMOUNT_FORMAT.format((0.01 / totalQuantity) * priceE2);
                break;
        }
        return text;
    }

    public static void setStartEnd(TextView startEnd, long start, long end, boolean capitalize) {
        if (DEFAULT_COLOR_STATE_LIST == null)
            DEFAULT_COLOR_STATE_LIST = startEnd.getTextColors();

        Calendar now = Calendar.getInstance(TIME_ZONE_GMT_PLUS_ONE);
        Calendar tomorrow = Calendar.getInstance(TIME_ZONE_GMT_PLUS_ONE);
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar startCalendar = Calendar.getInstance(TIME_ZONE_GMT_PLUS_ONE);
        startCalendar.setTimeInMillis(start);
        Calendar endCalendar = Calendar.getInstance(TIME_ZONE_GMT_PLUS_ONE);
        endCalendar.setTimeInMillis(end);

        int comparedWithStart = now.compareTo(startCalendar);
        int comparedWithEnd = now.compareTo(endCalendar);
        if (comparedWithStart < 0) {
            String text = capitalize ? "Starter " : "starter ";
            if (tomorrow.get(Calendar.DAY_OF_YEAR) == startCalendar.get(Calendar.DAY_OF_YEAR))
                startEnd.setText(text + "i morgen");
            else
                startEnd.setText(text + DATE_FORMAT.format(startCalendar.getTime()));
            startEnd.setTextColor(DEFAULT_COLOR_STATE_LIST);
        } else if (comparedWithEnd < 0) {
            String text = capitalize ? "Udløber " : "udløber ";
            if (now.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR))
                startEnd.setText(text + "i dag");
            else if (tomorrow.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR))
                startEnd.setText(text + "i morgen");
            else
                startEnd.setText(text + DATE_FORMAT.format(endCalendar.getTime()));
            startEnd.setTextColor(0xFF88FF88);
        } else {
            String text = capitalize ? "Udløb " : "udløb ";
            startEnd.setText(text + DATE_FORMAT.format(endCalendar.getTime()));
            startEnd.setTextColor(0xFFFF8888);
        }
    }

    /*
     * TextSizes:
     *      22.f    Large
     *      18.f    Medium
     *      14.f    Small
     */

    public static void setPrice(TextView price, TextView centis, int priceE2) {
        float textSize = price.getTextSize();
        price.setText("" + priceE2 / 100);
        int centisE2 = priceE2 - 100 * (priceE2 / 100);
        if (centisE2 == 0) {
            centis.setGravity(Gravity.CENTER_VERTICAL);
            centis.setTextSize(textSize);
            centis.setText(",-");
        } else {
            centis.setGravity(Gravity.TOP);
            centis.setTextSize(textSize - 6.f);
            centis.setText("" + centisE2);
        }
    }

    public static String getTypeSingular(short type) {
        String typeText = "";
        switch (type) {
            case Types.PIECE:
                typeText = "Ét styk";
                break;
            case Types.PACKAGE:
                typeText = "Én pakke";
                break;
            case Types.BAG:
                typeText = "Én pose";
                break;
            case Types.GLASS:
                typeText = "Ét glas";
                break;
            case Types.CAN:
                typeText = "Én dåse";
                break;
            case Types.BOTTLE:
                typeText = "Én flaske";
                break;
            case Types.BOX:
                typeText = "Én kasse";
                break;
            case Types.HALF_KILOGRAM:
                typeText = "Ét ½ kilo";
                break;
            case Types.TWO_PACK:
                typeText = "Én 2-pak";
                break;
            case Types.THREE_PACK:
                typeText = "Én 3-pak";
                break;
            case Types.FOUR_PACK:
                typeText = "Én 4-pak";
                break;
        }
        return typeText;
    }

    public static String getTypePlural(short type) {
        String typeText = "";
        switch (type) {
            case Types.PIECE:
                typeText = "styk";
                break;
            case Types.PACKAGE:
                typeText = "pakker";
                break;
            case Types.BAG:
                typeText = "poser";
                break;
            case Types.GLASS:
                typeText = "glas";
                break;
            case Types.CAN:
                typeText = "dåser";
                break;
            case Types.BOTTLE:
                typeText = "flasker";
                break;
            case Types.BOX:
                typeText = "kasser";
                break;
            case Types.HALF_KILOGRAM:
                typeText = "½ kilo";
                break;
            case Types.TWO_PACK:
                typeText = "2-pakker";
                break;
            case Types.THREE_PACK:
                typeText = "3-pakker";
                break;
            case Types.FOUR_PACK:
                typeText = "4-pakker";
                break;
        }
        return typeText;
    }

    public static String getQuantity(short quantity, short quantityTo, short quantityUnit) {
        String unitText = "";
        switch (quantityUnit) {
            case Units.LITER:
                unitText = "L";
                break;
            case Units.CENTILITER:
                if (quantity == 50 * (Math.round(quantity / 50))) {                 // dividable by 50 cl (½ L)?
                    if (quantityTo == 0)
                        return "" + LITER_FORMAT.format(quantity / 100.) + " L";
                    else if (quantityTo == 50 * (Math.round(quantityTo / 50)))
                        return "" + LITER_FORMAT.format(quantity / 100.) + " - " + LITER_FORMAT.format(quantityTo / 100.) + " L";
                    else
                        unitText = "cl";
                } else
                    unitText = "cl";
                break;
            case Units.MILLILITER:
                unitText = "ml";
                break;
            case Units.GRAM:
                unitText = "g";
                break;
            case Units.PIECE:
                unitText = "stk";
                break;
        }

        if (quantityTo > 0)
            return "" + quantity + " - " + quantityTo + " " + unitText;
        else
            return "" + quantity + " " + unitText;
    }
}
