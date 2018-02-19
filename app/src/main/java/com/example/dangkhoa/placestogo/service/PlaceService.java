package com.example.dangkhoa.placestogo.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.example.dangkhoa.placestogo.PlaceListFragment;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 29/09/2017.
 */

public class PlaceService extends IntentService {
    private static final String CLASS_NAME = "PlaceService";

    public static final String CURRENT_LOCATION_KEY = "current_location";
    public static final String RADIUS_KEY = "radius";
    public static final String PLACE_TYPE_KEY = "place_type_key";
    public static final String NEXT_PAGE_TOKEN_KEY = "next_page_token_key";

    public static final String PLACE_LIST_KEY_RESPONSE = "key_response";

    private ArrayList<PlaceDetail> placeList;
    private Location mLocation;

    // used to get next page token from the current request and send to main fragment
    private String nextPageTopen = null;

    public PlaceService() {
        super(CLASS_NAME);
        placeList = new ArrayList<>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String placeType = intent.getStringExtra(PLACE_TYPE_KEY);
        mLocation = intent.getParcelableExtra(CURRENT_LOCATION_KEY);
        String radius = intent.getStringExtra(RADIUS_KEY);
        String next_page_token = intent.getStringExtra(NEXT_PAGE_TOKEN_KEY);

        Uri uri = ServiceUtil.PlaceServiceContract.constructURI (
                getApplicationContext(),
                placeType,
                radius,
                new LatLng(mLocation.getLatitude(), mLocation.getLongitude()),
                next_page_token
        );

        String json = ServiceUtil.retrieveJson(uri);
        Log.d("MAIN", uri.toString());

        if (json != null) {
            placeList.addAll(extractJson(json));
        } else {
            return;
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(PlaceListFragment.PlaceServiceReceiver.PLACE_LIST_RECEIVER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putParcelableArrayListExtra(PLACE_LIST_KEY_RESPONSE, placeList);
        broadcastIntent.putExtra(NEXT_PAGE_TOKEN_KEY, nextPageTopen);
        sendBroadcast(broadcastIntent);
    }

    private ArrayList<PlaceDetail> extractJson(String json) {

        ArrayList<PlaceDetail> list = new ArrayList<>();

        try {
            JSONObject result = new JSONObject(json);
            JSONArray listRes = result.getJSONArray(ServiceUtil.RESULTS);

            if (result.has(ServiceUtil.NEXT_PAGE_TOKEN)) {
                nextPageTopen = result.getString(ServiceUtil.NEXT_PAGE_TOKEN);
            }

            if (listRes != null) {
                for (int i = 0; i < listRes.length(); i++) {

                    PlaceDetail placeDetail = new PlaceDetail();

                    JSONObject restaurantObj = listRes.getJSONObject(i);

                    JSONObject geometryObj = restaurantObj.getJSONObject(ServiceUtil.GEOMETRY);
                    JSONObject locationObj = geometryObj.getJSONObject(ServiceUtil.LOCATION);

                    Location placeLocation = new Location("");
                    placeLocation.setLatitude(locationObj.getDouble(ServiceUtil.LATITUDE));
                    placeLocation.setLongitude(locationObj.getDouble(ServiceUtil.LONGITUDE));

                    float distance = placeLocation.distanceTo(mLocation);
                    placeDetail.setDistance(String.valueOf(distance));

                    placeDetail.setLatitude(locationObj.getDouble(ServiceUtil.LATITUDE));
                    placeDetail.setLongitude(locationObj.getDouble(ServiceUtil.LONGITUDE));

                    placeDetail.setName(restaurantObj.getString(ServiceUtil.NAME));

                    // check if the opening_hours property exists
                    if (restaurantObj.has(ServiceUtil.OPENING_HOURS)) {
                        JSONObject openingHoursObj = restaurantObj.getJSONObject(ServiceUtil.OPENING_HOURS);
                        if (openingHoursObj.getBoolean(ServiceUtil.OPEN_NOW)) {
                            placeDetail.setOpening(1); // opening
                        } else {
                            placeDetail.setOpening(0); // closed
                        }
                    } else {
                        placeDetail.setOpening(1); // opening
                    }

                    placeDetail.setId(restaurantObj.getString(ServiceUtil.PLACE_ID));
                    placeDetail.setAddress(restaurantObj.getString(ServiceUtil.VICINITY));

                    // check if the rating property exists
                    if (restaurantObj.has(ServiceUtil.RATING)) {
                        placeDetail.setRating(restaurantObj.getDouble(ServiceUtil.RATING));
                    } else {
                        placeDetail.setRating(0.0);
                    }

                    // check if the photos property exists
                    if (restaurantObj.has(ServiceUtil.PHOTOS)) {
                        JSONArray photosArray = restaurantObj.getJSONArray(ServiceUtil.PHOTOS);
                        JSONObject photoObj = photosArray.getJSONObject(0);
                        placeDetail.setImage_url(Util.constructImageURL(getApplicationContext(), photoObj.getString(ServiceUtil.PHOTO_REFERENCE)));
                    }

                    list.add(placeDetail);
                }
            } else {
                // do nothing
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
