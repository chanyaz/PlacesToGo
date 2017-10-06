package com.example.dangkhoa.placestogo.service;

import android.content.Context;
import android.net.Uri;

import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Util;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by dangkhoa on 29/09/2017.
 */

public class ServiceUtil {
    public static final String RESULT = "result";
    public static final String RESULTS = "results";

    public static final String PLACE_ID = "place_id";

    public static final String NEXT_PAGE_TOKEN = "next_page_token";

    public static final String NAME = "name";
    public static final String LONG_NAME = "long_name";

    public static final String ADDRESS_COMPONENTS = "address_components";
    public static final String VICINITY = "vicinity";
    public static final String FORMATTED_ADDRESS = "formatted_address";
    public static final String POST_CODE = "postal_code";

    public static final String PHOTOS = "photos";
    public static final String PHOTO_REFERENCE = "photo_reference";

    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    public static final String TYPES = "types";

    public static final String OPENING_HOURS = "opening_hours";
    public static final String OPEN_NOW = "open_now";

    public static final String INTERNATIONAL_PHONE_NUMBER = "international_phone_number";
    public static final String WEBSITE = "website";

    public static final String REVIEWS = "reviews";
    public static final String AUTHOR_NAME = "author_name";
    public static final String PROFILE_PHOTO_URL = "profile_photo_url";
    public static final String RATING = "rating";
    public static final String TEXT = "text";
    public static final String RELATIVE_TIME_DESC = "relative_time_description";

    public static String retrieveJson(Uri uri) {

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        String json = null;

        try {

            URL url = new URL(uri.toString());

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            if (inputStream == null)
                return null;

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            json = stringBuilder.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return json;
    }

    public static String constructImageURL(Context context, String photo_ref) {
        String MAX_WIDTH = "300";
        String api = "&key=" + context.getString(R.string.android_api_key);
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + MAX_WIDTH + "&photoreference=" + photo_ref + api;
        return url;
    }

    public static class PlaceServiceContract {
        public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        public static final String API_KEY = "key";
        public static final String LOCATION = "location";
        public static final String TYPE = "type";
        public static final String S_RADIUS = "radius";
        public static final String NEXT_PAGE_TOKEN = "pagetoken";

        public static Uri constructURI(Context context, String placeType, String radius, LatLng location, String next_page_token) {
            Uri uri;

            if (next_page_token == null) {
                uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, context.getString(R.string.android_api_key))
                        .appendQueryParameter(S_RADIUS, radius)
                        .appendQueryParameter(TYPE, placeType)
                        .appendQueryParameter(LOCATION, Util.locationToString(location))
                        .build();
            } else {
                uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, context.getString(R.string.android_api_key))
                        .appendQueryParameter(S_RADIUS, radius)
                        .appendQueryParameter(TYPE, placeType)
                        .appendQueryParameter(LOCATION, Util.locationToString(location))
                        .appendQueryParameter(NEXT_PAGE_TOKEN, next_page_token)
                        .build();
            }

            return uri;
        }
    }

    public static class PlaceDetailServiceContract {
        public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
        public static final String PLACE_ID = "placeid";
        public static final String API_KEY = "key";

        public static Uri constructURI(Context context, String id) {
            Uri uri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, context.getString(R.string.android_api_key))
                    .appendQueryParameter(PLACE_ID, id)
                    .build();

            return uri;
        }
    }
}
