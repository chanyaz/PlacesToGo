package com.example.dangkhoa.placestogo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dangkhoa on 09/10/2017.
 */

public class PlacesDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "places.db";

    private static final int DATABASE_VERSION = 1;

    public PlacesDBHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_RESTAURANT_TABLE = "create table "
                + DBContract.PlacesEntry.TABLE_NAME
                + " ( "
                + DBContract.PlacesEntry.COLUMN_PLACE_ID + " text not null, "
                + DBContract.PlacesEntry.COLUMN_NAME + " text, "
                + DBContract.PlacesEntry.COLUMN_ADDRESS + " text, "
                + DBContract.PlacesEntry.COLUMN_IMAGE_URL + " text, "
                + DBContract.PlacesEntry.COLUMN_LATITUDE + " text, "
                + DBContract.PlacesEntry.COLUMN_LONGITUDE + " text, "
                + DBContract.PlacesEntry.COLUMN_RATING + " text, "
                + DBContract.PlacesEntry.COLUMN_LOCALITY + " text, "
                + DBContract.PlacesEntry.COLUMN_COUNTRY + " text, "
                + DBContract.PlacesEntry.COLUMN_POSTCODE + " text, "
                + DBContract.PlacesEntry.COLUMN_WEBSITE + " text, "
                + DBContract.PlacesEntry.COLUMN_PHONE + " text, "
                + DBContract.PlacesEntry.COLUMN_OPENING_HOURS + " text, "
                + "constraint " + DBContract.PlacesEntry.COLUMN_PLACE_ID + " primary key (" + DBContract.PlacesEntry.COLUMN_PLACE_ID + ")"
                + "on conflict replace );";

        sqLiteDatabase.execSQL(SQL_CREATE_RESTAURANT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBContract.PlacesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
