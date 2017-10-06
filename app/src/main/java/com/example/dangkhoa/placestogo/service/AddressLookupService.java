package com.example.dangkhoa.placestogo.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.dangkhoa.placestogo.MainActivity;
import com.example.dangkhoa.placestogo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class AddressLookupService extends IntentService {

    private static final String CLASS_NAME = "Address Lookup Service";

    public static final String CURRENT_LOCATION = "current_location";
    public static final String LOCATION_KEY_RESPONSE = "location key response";
    public static final String ERROR_MESSAGE_KEY_RESPONSE = "error message key response";

    private String formatted_address;
    private String errMessage = null;

    public AddressLookupService() {
        super(CLASS_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Location location = intent.getParcelableExtra(CURRENT_LOCATION);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            errMessage = getResources().getString(R.string.address_service_unavailable);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            errMessage = getResources().getString(R.string.invalid_lat_lon);
            e.printStackTrace();
        }

        if (addresses == null || addresses.size() == 0) {
            if (errMessage != null) {
                errMessage = getResources().getString(R.string.no_address_found);
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            formatted_address = TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.AddressLookupServiceReceiver.ADDRESS_RECEIVER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(LOCATION_KEY_RESPONSE, formatted_address);
        broadcastIntent.putExtra(ERROR_MESSAGE_KEY_RESPONSE, errMessage);
        sendBroadcast(broadcastIntent);
    }
}
