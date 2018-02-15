package com.example.dangkhoa.placestogo.Utils;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.database.DBContract;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 13/02/2018.
 */

public class SQLiteUtil {

    /**
     * Convert a PlaceDetail object to a ContentValues object to store in SQLite Database
     * @param placeDetail
     * @return
     */
    public static ContentValues valuesToDB(PlaceDetail placeDetail) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBContract.PlacesEntry.COLUMN_PLACE_ID, placeDetail.getId());
        contentValues.put(DBContract.PlacesEntry.COLUMN_NAME, placeDetail.getName());
        contentValues.put(DBContract.PlacesEntry.COLUMN_ADDRESS, placeDetail.getAddress());
        contentValues.put(DBContract.PlacesEntry.COLUMN_IMAGE_URL, placeDetail.getImage_url());
        contentValues.put(DBContract.PlacesEntry.COLUMN_LATITUDE, String.valueOf(placeDetail.getLatitude()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_LONGITUDE, String.valueOf(placeDetail.getLongitude()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_RATING, String.valueOf(placeDetail.getRating()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_LOCALITY, placeDetail.getLocality());
        contentValues.put(DBContract.PlacesEntry.COLUMN_COUNTRY, placeDetail.getCountry());
        contentValues.put(DBContract.PlacesEntry.COLUMN_POSTCODE, placeDetail.getPostCode());
        contentValues.put(DBContract.PlacesEntry.COLUMN_WEBSITE, placeDetail.getWebsite());
        contentValues.put(DBContract.PlacesEntry.COLUMN_PHONE, placeDetail.getInternationalPhone());
        contentValues.put(DBContract.PlacesEntry.COLUMN_OPENING_HOURS, openingHoursFromArrayListToString(placeDetail.getOpeningHours()));

        return contentValues;
    }

    /**
     * Retrieve a PlaceDetail object from SQLite Database
     * @param cursor
     * @return
     */
    public static PlaceDetail cursorPlace(Cursor cursor) {
        PlaceDetail placeDetail = new PlaceDetail();

        placeDetail.setId(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_PLACE_ID)));
        placeDetail.setName(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_NAME)));
        placeDetail.setAddress(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_ADDRESS)));
        placeDetail.setImage_url(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_IMAGE_URL)));
        placeDetail.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_LATITUDE))));
        placeDetail.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_LONGITUDE))));
        placeDetail.setRating(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_RATING))));
        placeDetail.setLocality(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_LOCALITY)));
        placeDetail.setCountry(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_COUNTRY)));
        placeDetail.setPostCode(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_POSTCODE)));
        placeDetail.setWebsite(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_WEBSITE)));
        placeDetail.setInternationalPhone(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_PHONE)));
        placeDetail.setOpeningHours(openingHoursFromStringToArrayList(cursor.getString(cursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_OPENING_HOURS))));

        return placeDetail;
    }

    /**
     * Convert opening hours list to a String to store in database
     * Data retrieved from API: "Monday: ...", "Tuesday: ..."
     * Converted String: "Monday: .../Tuesday: .../...."
     * @param list
     * @return
     */
    public static String openingHoursFromArrayListToString(ArrayList<String> list) {
        String openingHours = "";

        for (int i = 0; i < list.size(); i++) {
            openingHours += list.get(i);
            openingHours += "/";
        }

        return openingHours;
    }

    /**
     * Convert an opening hours String retrieved from SQLite Database into an ArrayList
     * @param openingHours
     * @return
     */
    public static ArrayList<String> openingHoursFromStringToArrayList(String openingHours) {
        String[] parts = openingHours.split("/");

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            list.add(parts[i]);
        }

        return list;
    }
}
