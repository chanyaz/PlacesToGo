package com.example.dangkhoa.placestogo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class Util {

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String locationToString(LatLng latLng) {
        return String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
    }

    public static String constructImageURL(Context context, String photo_ref) {
        String MAX_WIDTH = "300";
        String api = "&key=" + context.getString(R.string.android_api_key);
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + MAX_WIDTH + "&photoreference=" + photo_ref + api;
        return url;
    }

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

    public static void shareIntent(Context context, String message) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType(context.getResources().getString(R.string.share_type));
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_via)));
    }

    public static void createCallIntent(Context context, String phoneNumber) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("-", "")));
        context.startActivity(phoneIntent);
    }

    public static void createMapIntent(Context context, Double lat, Double lon, String label) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:<" + lat  + ">,<" + lon + ">?q=<" + lat  + ">,<" + lon + ">(" + label + ")"));
        context.startActivity(intent);
    }

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
}
