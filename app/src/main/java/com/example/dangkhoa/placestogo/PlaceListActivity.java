package com.example.dangkhoa.placestogo;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.Utils.FirebaseUtil;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class PlaceListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaceListFragment.PlacesCallback {

    private Location mLastLocation;
    private String mPlaceType;

    private ArrayList<PlaceDetail> mList;
    private float mRadius;

    private static final int RC_PHOTO_PICKER = 200;

    private static final String PHOTO_PICKER_TITLE = "Complete action using";
    private static final String PLACE_LIST_FRAGMENT_TAG = "place_list_fragment_tag";
    private static final String PLACE_LOCATION_SAVE_KEY = "place_location_save_key";
    private static final String PLACE_TYPE_SAVE_KEY = "place_type_save_key";
    private static final String MAP_FRAGMENT_TAG = "map_fragment_tag";

    public ActionBarDrawerToggle toggle;

    private FirebaseAuth mFirebaseAuth;

    private ViewHolder viewHolder;

    private class ViewHolder {

        public DrawerLayout drawerLayout;
        public NavigationView navigationView;
        public Toolbar toolbar;
        public TextView usernameTextView, emailTextView;
        public ImageView cameraButton, headerImage;

        public ViewHolder() {
            toolbar = findViewById(R.id.place_activity_toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            View headerView = navigationView.getHeaderView(0);
            usernameTextView = headerView.findViewById(R.id.usernameTextView);
            emailTextView = headerView.findViewById(R.id.emailTextView);
            headerImage = headerView.findViewById(R.id.nav_imageView);
            cameraButton = headerView.findViewById(R.id.nav_CameraButton);
        }
    }

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

        viewHolder = new ViewHolder();

        setSupportActionBar(viewHolder.toolbar);

        toggle = new ActionBarDrawerToggle(this, viewHolder.drawerLayout, viewHolder.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        viewHolder.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        viewHolder.navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mLastLocation = intent.getParcelableExtra(MainFragment.CURRENT_LOCATION_KEY);
        mPlaceType = intent.getStringExtra(MainFragment.PLACE_TYPE_KEY);

        mFirebaseAuth = FirebaseAuth.getInstance();

        viewHolder.usernameTextView.setText(mFirebaseAuth.getCurrentUser().getDisplayName());
        viewHolder.emailTextView.setText(mFirebaseAuth.getCurrentUser().getEmail());

        FirebaseUtil.setProfilePicture(getApplicationContext(), mFirebaseAuth, viewHolder.headerImage);

        viewHolder.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionsDialog();
            }
        });

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

    private void openOptionsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_options, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlide;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        LinearLayout selectPictureLayout = dialog.findViewById(R.id.select_picture_layout);
        LinearLayout takePictureLayout = dialog.findViewById(R.id.take_new_picture_layout);

        selectPictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhotoPicker();
                dialog.dismiss();
            }
        });

        takePictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, PHOTO_PICKER_TITLE), RC_PHOTO_PICKER);
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
        if (viewHolder.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
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
                FirebaseUtil.signOut(this);

                // navigate back to MainActivity to handle signing out
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);

                finish();
                break;
        }
        viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            FirebaseUtil.uploadProfilePicture(getApplicationContext(), mFirebaseAuth, selectedImage, viewHolder.headerImage);
        }
    }
}
