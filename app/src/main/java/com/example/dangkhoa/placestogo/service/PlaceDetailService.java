package com.example.dangkhoa.placestogo.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.dangkhoa.placestogo.DetailFragment;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.data.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 01/10/2017.
 */

public class PlaceDetailService extends IntentService {

    private static final String CLASS_NAME = "PlaceDetailService";

    public static String PLACE_ID_KEY = "place_id";

    public static String PLACE_DETAIL_RESPONSE_KEY = "place_detail_response_key";

    private PlaceDetail placeDetail;

    public PlaceDetailService() {
        super(CLASS_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        placeDetail = intent.getParcelableExtra(PLACE_ID_KEY);

        Uri uri = ServiceUtil.PlaceDetailServiceContract.constructURI(getApplicationContext(), placeDetail.getId());

        String json = ServiceUtil.retrieveJson(uri);

        Log.d("MAIN", uri.toString());

        if (json != null) {
            extractJson(json);
        } else {
            return;
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(DetailFragment.PlaceDetailServiceReceiver.PLACE_DETAIL_RECEIVER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PLACE_DETAIL_RESPONSE_KEY, placeDetail);
        sendBroadcast(broadcastIntent);
    }

    private void extractJson(String json) {

        boolean hasSetPostCode = false;

        try {
            JSONObject result = new JSONObject(json);
            JSONObject resultObj = result.getJSONObject(ServiceUtil.RESULT);

            placeDetail.setName(resultObj.getString(ServiceUtil.NAME));
            placeDetail.setAddress(resultObj.getString(ServiceUtil.VICINITY));

            if (resultObj.has(ServiceUtil.RATING)) {
                placeDetail.setRating(resultObj.getDouble(ServiceUtil.RATING));
            } else {
                placeDetail.setRating(0.0);
            }

            if (resultObj.has(ServiceUtil.PHOTOS)) {
                JSONArray photosArray = resultObj.getJSONArray(ServiceUtil.PHOTOS);
                JSONObject photoObj = photosArray.getJSONObject(0);
                placeDetail.setImage_url(ServiceUtil.constructImageURL(getApplicationContext(), photoObj.getString(ServiceUtil.PHOTO_REFERENCE)));
            }

            if (resultObj.has(ServiceUtil.OPENING_HOURS)) {
                JSONObject openingHoursObj = resultObj.getJSONObject(ServiceUtil.OPENING_HOURS);

                if (openingHoursObj.getBoolean(ServiceUtil.OPEN_NOW)) {
                    placeDetail.setOpening(1); // opening
                } else {
                    placeDetail.setOpening(0); // closed
                }

                if (openingHoursObj.has(ServiceUtil.WEEKDAY_TEXT)) {

                    JSONArray weekdayOpeningHours = openingHoursObj.getJSONArray(ServiceUtil.WEEKDAY_TEXT);

                    ArrayList<String> weekdayHours = new ArrayList<>();

                    for (int i = 0; i < weekdayOpeningHours.length(); i++) {
                        weekdayHours.add(weekdayOpeningHours.getString(i));
                    }
                    placeDetail.setOpeningHours(weekdayHours);
                }
            } else {
                placeDetail.setOpening(1); // opening
            }

            JSONObject geometryObj = resultObj.getJSONObject(ServiceUtil.GEOMETRY);
            JSONObject locationObj = geometryObj.getJSONObject(ServiceUtil.LOCATION);

            placeDetail.setLatitude(locationObj.getDouble(ServiceUtil.LATITUDE));
            placeDetail.setLongitude(locationObj.getDouble(ServiceUtil.LONGITUDE));


            JSONArray addressComponentsObj = resultObj.getJSONArray(ServiceUtil.ADDRESS_COMPONENTS);

            for (int i = 0; i < addressComponentsObj.length(); i++) {
                JSONObject addressObj = addressComponentsObj.getJSONObject(i);
                JSONArray types = addressObj.getJSONArray(ServiceUtil.TYPES);

                String firstType = types.getString(0);

                if (firstType.equals(ServiceUtil.POST_CODE)) {
                    placeDetail.setPostCode(addressObj.getString(ServiceUtil.LONG_NAME));
                    hasSetPostCode = true;
                    break;
                }
            }

            if (!hasSetPostCode) {
                placeDetail.setPostCode(null);
            }

            String formatted_address = resultObj.getString(ServiceUtil.FORMATTED_ADDRESS);
            setAddresses(formatted_address);

            placeDetail.setInternationalPhone(resultObj.getString(ServiceUtil.INTERNATIONAL_PHONE_NUMBER));

            if (resultObj.has(ServiceUtil.WEBSITE)) {
                placeDetail.setWebsite(resultObj.getString(ServiceUtil.WEBSITE));
            } else {
                placeDetail.setWebsite(null);
            }

            boolean hasSetReviews = false;
            if (resultObj.has(ServiceUtil.REVIEWS)) {
                JSONArray reviews = resultObj.getJSONArray(ServiceUtil.REVIEWS);

                ArrayList<Review> reviewArrayList = new ArrayList<>();

                for (int i = 0; i < reviews.length(); i++) {
                    JSONObject reviewObj = reviews.getJSONObject(i);

                    Review review = new Review();

                    review.setUser_name(reviewObj.getString(ServiceUtil.AUTHOR_NAME));
                    review.setReview_text(reviewObj.getString(ServiceUtil.TEXT));
                    review.setProfile_image_url(reviewObj.getString(ServiceUtil.PROFILE_PHOTO_URL));
                    review.setUser_rating(reviewObj.getString(ServiceUtil.RATING));
                    review.setReview_time_friendly(reviewObj.getString(ServiceUtil.RELATIVE_TIME_DESC));

                    reviewArrayList.add(review);
                }
                placeDetail.setReviews(reviewArrayList);
                hasSetReviews = true;
            }
            if (!hasSetReviews) {
                placeDetail.setReviews(null);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAddresses(String formatted_address) {

        String [] addresses = formatted_address.split(",");

        placeDetail.setCountry(addresses[addresses.length-1]);
        int count = 0;

        String locality = " ";

        for (int i = 0; i < addresses.length - 1; i++) {
            count++;
            locality += (addresses[i] + ", ");

            if (count == 2) {
                locality += "\n";
                count = 0;
            }
        }
        placeDetail.setLocality(locality);
    }
}
