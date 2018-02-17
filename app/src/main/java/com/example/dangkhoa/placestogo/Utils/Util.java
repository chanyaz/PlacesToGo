package com.example.dangkhoa.placestogo.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.database.DBContract;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class Util {

    /**
     * Convert a location LatLng object to String
     *
     * @param latLng
     * @return
     */
    public static String locationToString(LatLng latLng) {
        return String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
    }

    /**
     * Convert a String to a LatLng location object
     *
     * @param mStringLoc
     * @return
     */
    public static LatLng stringToLocation(String mStringLoc) {
        String[] strings = mStringLoc.split(",");
        LatLng location = new LatLng(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
        return location;
    }

    /**
     * Get photo URL from photo reference retrieved from Google Places API
     *
     * @param context
     * @param photo_ref
     * @return
     */
    public static String constructImageURL(Context context, String photo_ref) {
        String MAX_WIDTH = "300";
        String api = "&key=" + context.getString(R.string.android_api_key);
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + MAX_WIDTH + "&photoreference=" + photo_ref + api;
        return url;
    }

    /**
     * Convert a place type stored in strings.xml to a place label to be displayed
     *
     * @param placeType
     * @return
     */
    public static String placeTypeFromValueToLabel(String placeType) {
        String[] list = placeType.split("_");
        String label = "";

        for (int i = 0; i < list.length; i++) {
            String firstCharacter = list[i].substring(0, 1).toUpperCase();
            String remaining = list[i].substring(1);

            String recombineLabel = firstCharacter + remaining;

            label += recombineLabel;

            if (i < list.length - 1) {
                label += " ";
            }
        }
        return label;
    }

    /**
     * Create a share intent
     *
     * @param context
     * @param message
     */
    public static void shareIntent(Context context, String message) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType(context.getResources().getString(R.string.share_type));
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_via)));
    }

    /**
     * Create a call intent
     *
     * @param context
     * @param phoneNumber
     */
    public static void createCallIntent(Context context, String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("-", "")));
        context.startActivity(phoneIntent);
    }

    /**
     * Create a map intent
     *
     * @param context
     * @param lat
     * @param lon
     * @param label
     */
    public static void createMapIntent(Context context, Double lat, Double lon, String label) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:<" + lat + ">,<" + lon + ">?q=<" + lat + ">,<" + lon + ">(" + label + ")"));
        context.startActivity(intent);
    }

    /**
     * Request Uber
     *
     * @param context
     * @param lat
     * @param lon
     * @param dropOffName
     */
    public static void requestUber(final Context context, Double lat, Double lon, String dropOffName) {

        final String UBER_PACKAGE_NAME = "com.ubercab";

        try {

            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(UBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);

            String BASE_URI = "uber://?action=setPickup";
            String PICK_UP_LAT = "pickup[latitude]";
            String PICK_UP_LON = "pickup[longitude]";
            String PICK_UP_NICKNAME = "pickup[nickname]";
            String DROP_OFF_LAT = "dropoff[latitude]";
            String DROP_OFF_LON = "dropoff[longitude]";
            String DROP_OFF_NICKNAME = "dropoff[nickname]";

            String MY_LOCATION = "my_location";

            Uri uri = Uri.parse(BASE_URI).buildUpon()
                    .appendQueryParameter(PICK_UP_LAT, MY_LOCATION)
                    .appendQueryParameter(PICK_UP_LON, MY_LOCATION)
                    .appendQueryParameter(PICK_UP_NICKNAME, MY_LOCATION)
                    .appendQueryParameter(DROP_OFF_LAT, String.valueOf(lat))
                    .appendQueryParameter(DROP_OFF_LON, String.valueOf(lon))
                    .appendQueryParameter(DROP_OFF_NICKNAME, dropOffName)
                    .build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);

        } catch (PackageManager.NameNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getResources().getString(R.string.uber_requires_install))
                    .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = "https://play.google.com/store/apps/details?id=" + UBER_PACKAGE_NAME;
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            context.startActivity(intent);

                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Convert radius in mile and kilometer into meter
     *
     * @param context
     * @param radius
     * @param unit
     * @return
     */
    public static String radiusInMeter(Context context, float radius, String unit) {
        float kilometerConst = 1000f;
        float mileConst = 1609.34f;

        if (unit.equals(context.getString(R.string.pref_unit_miles_value))) {
            return String.valueOf(radius * mileConst);
        } else {
            return String.valueOf(radius * kilometerConst);
        }
    }

    /**
     * Get current time
     *
     * @return
     */
    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy"; // 24 hours

    public static String getCurrentTime() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        return simpleDateFormat.format(currentTime);
    }

    /**
     * Check if a formatted String is a valid date
     *
     * @param inDate
     * @return
     */
    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    /**
     * Return a time friendly message
     *
     * @param context
     * @param receivedTime
     * @return
     */
    public static String timeFriendly(Context context, String receivedTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        String currentTime = Util.getCurrentTime();

        String timeDiff = "";

        try {
            Date currentDate = dateFormat.parse(currentTime);
            Date receivedDate = dateFormat.parse(receivedTime);

            long diff = currentDate.getTime() - receivedDate.getTime();

            //long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffMinutes == 0 && diffHours == 0 && diffDays == 0) {
                timeDiff = context.getString(R.string.seconds_ago);

            } else if (diffHours == 0 && diffDays == 0) {

                if (diffMinutes == 1) {
                    timeDiff = context.getString(R.string.a_minute_ago);
                } else {
                    timeDiff = diffMinutes + " " + context.getString(R.string.minutes_ago);
                }

            } else if (diffDays == 0) {

                if (diffHours == 1) {
                    timeDiff = context.getString(R.string.an_hour_ago);
                } else {
                    timeDiff = diffHours + " " + context.getString(R.string.hours_ago);
                }

            } else if (diffDays > 0 && diffDays <= 7) {

                if (diffDays == 1) {
                    timeDiff = context.getString(R.string.a_day_ago);
                } else {
                    timeDiff = diffDays + " " + context.getString(R.string.days_ago);
                }

            } else if (diffDays > 7 && diffDays <= 30) {

                int weeks = (int) diffDays / 7;

                if (weeks == 1) {
                    timeDiff = context.getString(R.string.a_week_ago);
                } else {
                    timeDiff = weeks + " " + context.getString(R.string.weeks_ago);
                }

            } else if (diffDays > 30 && diffDays <= 365) {

                int months = (int) diffDays / 30;

                if (months == 1) {
                    timeDiff = context.getString(R.string.a_month_ago);
                } else {
                    timeDiff = months + " " + context.getString(R.string.months_ago);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeDiff;
    }
}
