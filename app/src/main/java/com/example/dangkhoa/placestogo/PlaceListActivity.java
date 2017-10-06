package com.example.dangkhoa.placestogo;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class PlaceListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Location mLastLocation;
    private String mPlaceType;

    private static final String PLACE_LIST_FRAGMENT_TAG = "place_list_fragment_tag";

    private static final String PLACE_LOCATION_SAVE_KEY = "place_location_save_key";
    private static final String PLACE_TYPE_SAVE_KEY = "place_type_save_key";

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
        mLastLocation = intent.getParcelableExtra(MainActivity.CURRENT_LOCATION_KEY);
        mPlaceType = intent.getStringExtra(MainActivity.PLACE_TYPE_KEY);

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
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
