package com.konzoomer;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.konzoomer.db.KonzoomerDatabaseHelper;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 03-12-2010
 * Time: 15:06:59
 */
public class OfferProvider extends ContentProvider {

//    String TAG = "OfferProvider";

    public static String AUTHORITY = "com.konzoomer.OfferProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/offer");

    private static final int SEARCH_SUGGEST = 0;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
//        matcher.addURI(AUTHORITY, "dictionary", SEARCH_WORDS);
//        matcher.addURI(AUTHORITY, "dictionary/#", GET_WORD);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
//        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
//        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Handles all the dictionary searches and suggestion queries from the Search Manager.
     * When requesting a specific word, the uri alone is required.
     * When searching all of the dictionary for matches, the selectionArgs argument must carry
     * the search query as the first element.
     * All other arguments are ignored.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
/*
        Log.i(TAG, "query uri=" + uri);
        Log.i(TAG, "projection=" + (projection == null ? "null" : Arrays.asList(projection)));
        Log.i(TAG, "selection=" + selection);
        Log.i(TAG, "selectionArgs=" + (selectionArgs == null ? "null" : Arrays.asList(selectionArgs)));
        Log.i(TAG, "sortOrder=" + sortOrder);

        Log.i(TAG, "uri.lastPathSegment=" + uri.getLastPathSegment());
        Log.i(TAG, "uri.query=" + uri.getQuery());
*/
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                String query = "";
                if(!SearchManager.SUGGEST_URI_PATH_QUERY.equals(uri.getLastPathSegment()))
                    query = uri.getLastPathSegment();

                return getSuggestions(query);
/*            case SEARCH_WORDS:
                if (selectionArgs == null) {
                  throw new IllegalArgumentException(
                      "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_WORD:
                return getWord(uri);
            case REFRESH_SHORTCUT:
                return refreshShortcut(uri);*/
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
/*      String[] columns = new String[] {
          BaseColumns._ID,
          DictionaryDatabase.KEY_WORD,
          DictionaryDatabase.KEY_DEFINITION,
//        SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
//                        (only if you want to refresh shortcuts)
          SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID};

      return mDictionary.getWordMatches(query, columns);
*/

        // UnitQuantities
        // Name words
        // Description words

        // Brands
        return Konzoomer.getDB().rawQuery("SELECT _id" +
                ", name AS " + SearchManager.SUGGEST_COLUMN_QUERY +
                ", name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 +
                ", \"Varemærke\" AS " + SearchManager.SUGGEST_COLUMN_TEXT_2 +
                " FROM " + KonzoomerDatabaseHelper.BRAND_TABLE_NAME + " WHERE name LIKE \"%" + query + "%\"", null);
    }

    /**
     * This method is required in order to query the supported types.
     * It's also useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
/*            case SEARCH_WORDS:
                return WORDS_MIME_TYPE;
            case GET_WORD:
                return DEFINITION_MIME_TYPE;*/
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
/*            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;*/
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
