package com.example.dangkhoa.placestogo.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by dangkhoa on 09/10/2017.
 */

public class PlacesContentProvider extends ContentProvider {

    public static final int PLACES = 100;
    public static final int PLACES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // add uri matcher with addURI(String authority, String path, int code)
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_PLACES, PLACES);
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_PLACES + "/*", PLACES_WITH_ID);

        return uriMatcher;
    }

    private PlacesDBHelper mPlacesDBHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mPlacesDBHelper = new PlacesDBHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mPlacesDBHelper.getReadableDatabase();

        final int matcher = sUriMatcher.match(uri);

        Cursor returnCursor;

        switch (matcher) {
            case PLACES:
                returnCursor = db.query(DBContract.PlacesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case PLACES_WITH_ID:
                // using selection and selectionArgs
                // URI: content://<authority>/places/*
                // index 0 is the "places" portion of the path. index 1 is the part next to it, which is the id passed in
                String id = uri.getPathSegments().get(1);

                String mSelection = DBContract.PlacesEntry.COLUMN_PLACE_ID + "=?";
                String[] mSelectionArgs = new String[] {id};

                returnCursor = db.query(DBContract.PlacesEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mPlacesDBHelper.getWritableDatabase();

        final int matcher = sUriMatcher.match(uri);

        Uri returnUri;

        switch (matcher) {
            case PLACES:
                long id = db.insert(DBContract.PlacesEntry.TABLE_NAME, null, contentValues);

                if (id > 0) {
                    // if insertion is successful, return an uri with row id
                    // pass in a content_uri with an inserted row id
                    returnUri = ContentUris.withAppendedId(DBContract.PlacesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Insert failed into " + uri);
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // notify the resolver if the uri has been changed
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mPlacesDBHelper.getWritableDatabase();

        final int matcher = sUriMatcher.match(uri);

        int placeDeleted;

        switch (matcher) {
            case PLACES_WITH_ID:
                String id = uri.getPathSegments().get(1);

                String mSelection = DBContract.PlacesEntry.COLUMN_PLACE_ID + "=?";

                String[] mSelectionArgs = new String[] {id};

                placeDeleted = db.delete(DBContract.PlacesEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (placeDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return placeDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        // we are not going to update any information about place
        // so, just leave this function empty
        return 0;
    }
}
