package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangkhoa.placestogo.adapter.GooglePlacesAutoCompleteAdapter;
import com.example.dangkhoa.placestogo.service.AddressLookupService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "MAIN";

    private class ViewHolder {

        public DrawerLayout drawerLayout;
        public NavigationView navigationView;
        public ActionBarDrawerToggle toggle;
        public Toolbar toolbar;

        public AutoCompleteTextView searchTextView;
        public ImageButton locationButton;
        public TextView userLocationTextView;

        public TextView restaurantText, cafeText, takeawayText, storeText, busText;

        public LinearLayout restaurantLayout, cafeLayout, takeawayLayout, storeLayout, busStationLayout, moreLayout;

        public ViewHolder() {
            toolbar = findViewById(R.id.main_toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            searchTextView = findViewById(R.id.autoCompleteTextView);
            locationButton = findViewById(R.id.location_button);
            userLocationTextView = findViewById(R.id.user_location_text);

            restaurantLayout = findViewById(R.id.restaurant_item_layout);
            cafeLayout = findViewById(R.id.cafe_item_layout);
            takeawayLayout = findViewById(R.id.takeaway_item_layout);
            storeLayout = findViewById(R.id.store_item_layout);
            busStationLayout = findViewById(R.id.bus_station_item_layout);
            moreLayout = findViewById(R.id.more_item_layout);

            restaurantText = findViewById(R.id.restaurant_item_text);
            cafeText = findViewById(R.id.cafe_item_text);
            takeawayText = findViewById(R.id.takeaway_item_text);
            storeText = findViewById(R.id.store_item_text);
            busText = findViewById(R.id.bus_station_item_text);
        }
    }

    private static final int REQUEST_LOCATION = 100;

    private ViewHolder viewHolder;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    private boolean mRequestingLocationUpdates;

    private String mLastAddress;
    private String mLastErrorMessage;

    private AddressLookupServiceReceiver receiver;

    private final String LOCATION_SAVE_KEY = "location_save_key";
    private final String REQUESTING_LOCATION_STATE_SAVE_KEY = "requesting_location_state_save_key";
    private final String ERROR_MESSAGE_SAVE_KEY = "error_message_save_key";

    private final String FRAGMENT_PLACE_TYPE_TAG = "fragment_place_type_tag";

    // store place type value
    private String mPlaceType;

    // keys to send package to PlaceListActivity
    public static final String PLACE_TYPE_KEY = "place_type_key";
    public static final String CURRENT_LOCATION_KEY = "current_location_key";

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelable(LOCATION_SAVE_KEY, mLastLocation);
        outState.putBoolean(REQUESTING_LOCATION_STATE_SAVE_KEY, mRequestingLocationUpdates);
        outState.putString(ERROR_MESSAGE_SAVE_KEY, mLastErrorMessage);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // prevent keyboard from showing up when user not click on the search area
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (savedInstanceState != null) {
            mLastLocation = savedInstanceState.getParcelable(LOCATION_SAVE_KEY);
            mLastErrorMessage = savedInstanceState.getString(ERROR_MESSAGE_SAVE_KEY);
            mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_STATE_SAVE_KEY);
        } else {
            mLastLocation = null;
            mLastErrorMessage = null;
            mRequestingLocationUpdates = true;
        }

        viewHolder = new ViewHolder();

        IntentFilter intentFilterAddress = new IntentFilter(AddressLookupServiceReceiver.ADDRESS_RECEIVER);
        intentFilterAddress.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new AddressLookupServiceReceiver();
        registerReceiver(receiver, intentFilterAddress);

        buildGoogleApiClient();

        setSupportActionBar(viewHolder.toolbar);

        viewHolder.toggle = new ActionBarDrawerToggle(this, viewHolder.drawerLayout, viewHolder.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        viewHolder.drawerLayout.addDrawerListener(viewHolder.toggle);
        viewHolder.toggle.syncState();

        viewHolder.navigationView.setNavigationItemSelectedListener(this);

        viewHolder.searchTextView.setAdapter(new GooglePlacesAutoCompleteAdapter(this));

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
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag(FRAGMENT_PLACE_TYPE_TAG);

                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                Bundle arguments = new Bundle();
                arguments.putParcelable(CURRENT_LOCATION_KEY, mLastLocation);

                FragmentPlaceType fragmentPlaceType = new FragmentPlaceType();
                fragmentPlaceType.setArguments(arguments);
                fragmentPlaceType.show(ft, FRAGMENT_PLACE_TYPE_TAG);
            }
        });
    }

    private void startPlaceListActivity() {
        // some times location returns null
        // therefore, when place list activity starts, app crashes due to null location
        if (mLastLocation == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show();
            // after informing users, we should start getting location again so that users don't need to do this themselves
            startLocationUpdate();
        } else {
            Intent intent = new Intent(this, PlaceListActivity.class);
            intent.putExtra(PLACE_TYPE_KEY, mPlaceType);
            intent.putExtra(CURRENT_LOCATION_KEY, mLastLocation);
            startActivity(intent);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_favorite:
                // open favorite activity
                break;
            case R.id.menu_settings:
                // open setting activity
                break;
            case R.id.menu_maps:
                // open google maps
                break;
            case R.id.menu_share:
                String message = this.getResources().getString(R.string.app_share);
                Util.shareIntent(this, message);
                break;
        }
        viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopLocationUpdate() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } catch (IllegalStateException e) {

        }
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            if (!Util.checkInternetConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
            } else {
                createLocationRequest();
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    private void startAddressLookupService() {
        Intent intent = new Intent(this, AddressLookupService.class);
        intent.putExtra(AddressLookupService.CURRENT_LOCATION, mLastLocation);
        startService(intent);
    }

    private void updateUI() {
        if (mLastErrorMessage == null) {
            viewHolder.userLocationTextView.setText(mLastAddress);
        } else {
            viewHolder.userLocationTextView.setText(mLastErrorMessage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
        viewHolder.searchTextView.setText("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            // Disconnect the client
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Make sure location request is not called when the app is resumed
        if (mLastLocation == null && mRequestingLocationUpdates) {
            startLocationUpdate();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, getResources().getString(R.string.connection_suspended), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getResources().getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(this.getResources().getString(R.string.request_location_permission_message))
                    .setPositiveButton(this.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, this.getResources().getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show();
                    startLocationUpdate();
                } else {
                    Toast.makeText(this, this.getResources().getString(R.string.location_permission_not_granted), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onBackPressed() {
        if (viewHolder.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (!viewHolder.searchTextView.getText().toString().isEmpty()) {
                viewHolder.searchTextView.setText("");
            } else {
                super.onBackPressed();
            }
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

}
