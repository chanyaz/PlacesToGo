package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangkhoa.placestogo.Utils.NetworkUtil;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.service.AddressLookupService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "MAIN_FRAGMENT";

    private class ViewHolder {

        public ImageButton locationButton;

        public TextView userLocationTextView;
        public TextView restaurantText, cafeText, takeawayText, storeText, busText;

        public LinearLayout restaurantLayout, cafeLayout, takeawayLayout, storeLayout, busStationLayout, moreLayout;

        public ViewHolder(View view) {

            locationButton = view.findViewById(R.id.location_button);
            userLocationTextView = view.findViewById(R.id.user_location_text);

            restaurantLayout = view.findViewById(R.id.restaurant_item_layout);
            cafeLayout = view.findViewById(R.id.cafe_item_layout);
            takeawayLayout = view.findViewById(R.id.takeaway_item_layout);
            storeLayout = view.findViewById(R.id.store_item_layout);
            busStationLayout = view.findViewById(R.id.bus_station_item_layout);
            moreLayout = view.findViewById(R.id.more_item_layout);

            restaurantText = view.findViewById(R.id.restaurant_item_text);
            cafeText = view.findViewById(R.id.cafe_item_text);
            takeawayText = view.findViewById(R.id.takeaway_item_text);
            storeText = view.findViewById(R.id.store_item_text);
            busText = view.findViewById(R.id.bus_station_item_text);
        }
    }

    private static final int REQUEST_LOCATION = 100;

    private ViewHolder viewHolder;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private String mLastAddress;
    private String mLastErrorMessage;

    private boolean requestLocationOnNetworkReconnected;

    private AddressLookupServiceReceiver addressReceiver;
    private NetworkChangeReceiver networkChangeReceiver;

    // keys to send package to PlaceListActivity
    public static final String PLACE_TYPE_KEY = "place_type_key";
    public static final String CURRENT_LOCATION_KEY = "current_location_key";

    private static final String REQUEST_SERVICE_ON_NETWORK_RECONNECTED = "request on reconnected";

    private static final String LAST_ADDRESS_SAVE_KEY = "last address save key";
    private static final String LAST_LOCATION_SAVE_KEY = "last location save key";

    // store place type value
    private String mPlaceType;

    interface MorePlacesCallback {
        // callback to MainActivity to load PlaceTypeList fragment
        void displayFragmentPlaceTypeList(Location location);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUEST_SERVICE_ON_NETWORK_RECONNECTED, requestLocationOnNetworkReconnected);
        outState.putString(LAST_ADDRESS_SAVE_KEY, mLastAddress);
        outState.putParcelable(LAST_LOCATION_SAVE_KEY, mLastLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prevent keyboard from showing up when user not click on the search area
        ((AppCompatActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (savedInstanceState != null) {
            requestLocationOnNetworkReconnected = savedInstanceState.getBoolean(REQUEST_SERVICE_ON_NETWORK_RECONNECTED);
            mLastAddress = savedInstanceState.getString(LAST_ADDRESS_SAVE_KEY);
            mLastLocation = savedInstanceState.getParcelable(LAST_LOCATION_SAVE_KEY);
        } else {
            mLastAddress = null;
            mLastLocation = null;
            mLastErrorMessage = null;
            requestLocationOnNetworkReconnected = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        viewHolder = new ViewHolder(view);

        // register for Address Receiver Service
        IntentFilter intentFilterAddress = new IntentFilter(AddressLookupServiceReceiver.ADDRESS_RECEIVER);
        intentFilterAddress.addCategory(Intent.CATEGORY_DEFAULT);
        addressReceiver = new AddressLookupServiceReceiver();
        getContext().registerReceiver(addressReceiver, intentFilterAddress);

        // register for Network Receiver Service
        IntentFilter intentFilterNetwork = new IntentFilter();
        intentFilterNetwork.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        getContext().registerReceiver(networkChangeReceiver, intentFilterNetwork);

        viewHolder.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdate();
            }
        });

        viewHolder.restaurantText.setText(Util.placeTypeFromValueToLabel(getResources().getString(R.string.restaurant_value)));
        viewHolder.cafeText.setText(Util.placeTypeFromValueToLabel(getResources().getString(R.string.cafe_value)));
        viewHolder.takeawayText.setText(Util.placeTypeFromValueToLabel(getResources().getString(R.string.meal_takeaway_value)));
        viewHolder.storeText.setText(Util.placeTypeFromValueToLabel(getResources().getString(R.string.store_value)));
        viewHolder.busText.setText(Util.placeTypeFromValueToLabel(getResources().getString(R.string.bus_station_value)));

        viewHolder.restaurantLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceType = getResources().getString(R.string.restaurant_value);
                startPlaceListActivity();
            }
        });

        viewHolder.cafeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceType = getResources().getString(R.string.cafe_value);
                startPlaceListActivity();
            }
        });

        viewHolder.takeawayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceType = getResources().getString(R.string.meal_takeaway_value);
                startPlaceListActivity();
            }
        });

        viewHolder.storeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceType = getResources().getString(R.string.convenience_store_value);
                startPlaceListActivity();
            }
        });

        viewHolder.busStationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaceType = getResources().getString(R.string.bus_station_value);
                startPlaceListActivity();
            }
        });

        viewHolder.moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MorePlacesCallback) getContext()).displayFragmentPlaceTypeList(mLastLocation);
            }
        });

        if (mLastAddress == null) {
            buildGoogleApiClient();
        } else {
            updateUI();
        }

        return view;
    }

    private void startPlaceListActivity() {
        // some times location returns null
        // therefore, when place list activity starts, app crashes due to null location
        if (mLastLocation == null) {
            Toast.makeText(getContext(), getResources().getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
            // after informing users, we should start getting location again so that users don't need to do this themselves
            if (mGoogleApiClient != null) {
                startLocationUpdate();
            }
        } else {
            Intent intent = new Intent(getContext(), PlaceListActivity.class);
            intent.putExtra(PLACE_TYPE_KEY, mPlaceType);
            intent.putExtra(CURRENT_LOCATION_KEY, mLastLocation);
            startActivity(intent);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopLocationUpdate() {
        if (mGoogleApiClient != null) {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            } catch (IllegalStateException e) {

            }
        }
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermission();

        } else {
            if (!NetworkUtil.checkInternetConnection(getContext()) && !requestLocationOnNetworkReconnected) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                builder.setMessage(getContext().getString(R.string.location_load_no_internet_connection))
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // set this variable to true so that when user turns on internet MANUALLY, location will be automatically detected
                                requestLocationOnNetworkReconnected = true;
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // turn on wifi
                                NetworkUtil.setWifiState(getContext(), true);
                                requestLocationOnNetworkReconnected = true;

                                viewHolder.userLocationTextView.setText(getString(R.string.detecting_location));
                            }
                        });

                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                viewHolder.userLocationTextView.setText(getString(R.string.detecting_location));

                if (mGoogleApiClient != null) {
                    createLocationRequest();
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                } else {
                    buildGoogleApiClient();
                }
            }
        }
    }

    private void startAddressLookupService() {
        Intent intent = new Intent(getContext(), AddressLookupService.class);
        intent.putExtra(AddressLookupService.CURRENT_LOCATION, mLastLocation);
        getContext().startService(intent);
    }

    private void updateUI() {
        if (mLastErrorMessage == null) {
            viewHolder.userLocationTextView.setText(mLastAddress);
        } else {
            viewHolder.userLocationTextView.setText(mLastErrorMessage);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client
        if (mLastAddress == null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                // Disconnect the client
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(addressReceiver);
        getContext().unregisterReceiver(networkChangeReceiver);
        stopLocationUpdate();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Make sure location request is not called when the app is resumed
        if (mLastLocation == null) {
            startLocationUpdate();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), getResources().getString(R.string.connection_suspended), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), getResources().getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLastLocation = location;
            startAddressLookupService();
        }
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(this.getResources().getString(R.string.request_location_permission_message))
                    .setPositiveButton(this.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), getResources().getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show();
                    startLocationUpdate();
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.location_permission_not_granted), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public class AddressLookupServiceReceiver extends BroadcastReceiver {
        public static final String ADDRESS_RECEIVER = "com.example.android.placestogo.address";

        @Override
        public void onReceive(Context context, Intent intent) {
            String address = intent.getStringExtra(AddressLookupService.LOCATION_KEY_RESPONSE);
            String errorMessage = intent.getStringExtra(AddressLookupService.ERROR_MESSAGE_KEY_RESPONSE);

            mLastAddress = address;
            mLastErrorMessage = errorMessage;

            updateUI();
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            int status = NetworkUtil.getConnectivityStatusString(context);

            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

            } else {
                if (requestLocationOnNetworkReconnected) {

                    if (mGoogleApiClient != null) {
                        startLocationUpdate();
                    } else {
                        buildGoogleApiClient();
                    }

                }
            }
        }
    }

}
