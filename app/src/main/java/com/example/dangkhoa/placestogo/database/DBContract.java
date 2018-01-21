package com.example.dangkhoa.placestogo.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by dangkhoa on 09/10/2017.
 */

public class DBContract {

    public static final String AUTHORITY = "com.example.dangkhoa.placestogo";

    // base content uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // path to places table:
    // content://com.example.dangkhoa.placestogo/places
    public static final String PATH_PLACES = "places";

    public static final class PlacesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        // database table
        public static final String TABLE_NAME = "places_table";
        public static final String COLUMN_PLACE_ID = "place_id";
        public static final String COLUMN_NAME = "place_name";
        public static final String COLUMN_ADDRESS = "place_address";
        public static final String COLUMN_IMAGE_URL = "place_image_url";
        public static final String COLUMN_LATITUDE = "place_lat";
        public static final String COLUMN_LONGITUDE = "place_lon";
        public static final String COLUMN_RATING = "place_rating";
        public static final String COLUMN_LOCALITY = "place_locality";
        public static final String COLUMN_COUNTRY = "place_country";
        public static final String COLUMN_POSTCODE = "place_post_code";
        public static final String COLUMN_WEBSITE = "place_website";
        public static final String COLUMN_PHONE = "place_phone";

        public static Uri buildItemUri(String _id) {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).appendPath(_id).build();
        }
    }
}
