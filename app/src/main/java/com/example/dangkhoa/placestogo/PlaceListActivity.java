package com.example.dangkhoa.placestogo;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PlaceListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaceListFragment.PlacesCallback {

    private Location mLastLocation;
    private String mPlaceType;

    private ArrayList<PlaceDetail> mList;
    private float mRadius;

    private static final String PLACE_LIST_FRAGMENT_TAG = "place_list_fragment_tag";

    private static final String PLACE_LOCATION_SAVE_KEY = "place_location_save_key";
    private static final String PLACE_TYPE_SAVE_KEY = "place_type_save_key";

    private static final String MAP_FRAGMENT_TAG = "map_fragment_tag";

    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ActionBarDrawerToggle toggle;
    public Toolbar toolbar;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PLACE_LOCATION_SAVE_KEY, mLastLocation);
        outState.putString(PLACE_TYPE_SAVE_KEY, mPlaceType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        toolbar = findViewById(R.id.place_activity_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mLastLocation = intent.getParcelableExtra(MainFragment.CURRENT_LOCATION_KEY);
        mPlaceType = intent.getStringExtra(MainFragment.PLACE_TYPE_KEY);

        if (savedInstanceState == null) {

            Bundle bundle = new Bundle();
            bundle.putParcelable(PlaceListFragment.LOCATION_BUNDLE_KEY, mLastLocation);
            bundle.putString(PlaceListFragment.PLACE_TYPE_BUNDLE_KEY, mPlaceType);

            PlaceListFragment placeListFragment = new PlaceListFragment();
            placeListFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_place_list_container, placeListFragment, PLACE_LIST_FRAGMENT_TAG)
                    .commit();
        } else {
            mLastLocation = savedInstanceState.getParcelable(PLACE_LOCATION_SAVE_KEY);
            mPlaceType = savedInstanceState.getString(PLACE_TYPE_SAVE_KEY);
        }
        getSupportActionBar().setTitle(Util.placeTypeFromValueToLabel(mPlaceType));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapPlaceList:
                // open map fragment
                if (mList != null && mList.size() > 0) {
                    MapFragment checkMapFragment = (MapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);

                    if (checkMapFragment == null) {
                        LatLng mLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                        Bundle args = new Bundle();
                        args.putFloat(MapFragment.RADIUS_KEY, mRadius);
                        args.putString(MapFragment.LOCATION_KEY, Util.locationToString(mLocation));
                        args.putParcelableArrayList(MapFragment.PLACE_LIST_KEY, mList);

                        MapFragment mapFragment = new MapFragment();
                        mapFragment.setArguments(args);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        PlaceListFragment prevPlaceListFragment = (PlaceListFragment) getFragmentManager().findFragmentByTag(PLACE_LIST_FRAGMENT_TAG);

                        if (prevPlaceListFragment != null) {
                            fragmentTransaction.hide(prevPlaceListFragment);
                        }
                        fragmentTransaction.add(R.id.activity_place_list_container, mapFragment, MAP_FRAGMENT_TAG).commit();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlaceListActivity.this);
                    builder.setMessage(getString(R.string.no_places_to_be_displayed_in_map))
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
            PlaceListFragment placeListFragment = (PlaceListFragment) getFragmentManager().findFragmentByTag(PLACE_LIST_FRAGMENT_TAG);

            if (mapFragment != null && !placeListFragment.isVisible()) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                fragmentTransaction.remove(mapFragment);

                fragmentTransaction.show(placeListFragment);
                fragmentTransaction.commit();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_favorite:
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                // open setting activity
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_maps:
                // open google maps
                break;
            case R.id.menu_share:
                String message = this.getResources().getString(R.string.app_share);
                Util.shareIntent(this, message);
                break;
            case R.id.menu_account:
                break;
            case R.id.menu_signout:
                Util.signOut(this);
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void radiusAchieved(float radius) {
        mRadius = radius;
    }

    @Override
    public void placesListAchieved(ArrayList<PlaceDetail> list) {
        mList = list;
    }
}
