package com.konzoomer.domain;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 25-11-2010
 * Time: 00:59:36
 */
public interface Units {

    public static final byte LITER = 1;
    public static final byte CENTILITER = 2;
    public static final byte MILLILITER = 3;
    public static final byte GRAM = 4;
    public static final byte PIECE = 5;         // Used for e.g. diaper-packages - how many diapers does the offer_detail package(s) contain?
}
