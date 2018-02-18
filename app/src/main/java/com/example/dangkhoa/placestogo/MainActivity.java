package com.example.dangkhoa.placestogo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.Utils.FirebaseUtil;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.adapter.GlideApp;
import com.example.dangkhoa.placestogo.adapter.GooglePlacesAutoCompleteAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainFragment.MorePlacesCallback {

    private static final String LOG_TAG = "MAIN";
    private static final int RC_SIGN_IN = 100;
    private static final int RC_PHOTO_PICKER = 200;

    private static final String PHOTO_PICKER_TITLE = "Complete action using";
    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private static final String PLACE_TYPE_FRAGMENT_TAG = "place_type_fragment_tag";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private class ViewHolder {

        public DrawerLayout drawerLayout;
        public NavigationView navigationView;
        public ActionBarDrawerToggle toggle;
        public Toolbar toolbar;
        public TextView usernameTextView, emailTextView;

        public ImageView cameraButton, headerImage;

        public ListView searchListView;

        public SearchView searchView;

        public ViewHolder() {
            toolbar = findViewById(R.id.main_toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            searchListView = findViewById(R.id.searchListView);

            View headerView = navigationView.getHeaderView(0);
            usernameTextView = headerView.findViewById(R.id.usernameTextView);
            emailTextView = headerView.findViewById(R.id.emailTextView);

            headerImage = headerView.findViewById(R.id.nav_imageView);
            cameraButton = headerView.findViewById(R.id.nav_CameraButton);
        }
    }

    private ViewHolder viewHolder;

    private GooglePlacesAutoCompleteAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        viewHolder = new ViewHolder();

        setSupportActionBar(viewHolder.toolbar);

        mSearchAdapter = new GooglePlacesAutoCompleteAdapter(this);
        viewHolder.searchListView.setAdapter(mSearchAdapter);

        viewHolder.toggle = new ActionBarDrawerToggle(this, viewHolder.drawerLayout, viewHolder.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        viewHolder.drawerLayout.addDrawerListener(viewHolder.toggle);
        viewHolder.toggle.syncState();

        viewHolder.navigationView.setNavigationItemSelectedListener(this);

        viewHolder.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionsDialog();
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {

                    MainFragment mainFragment = (MainFragment) getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);

                    if (mainFragment == null) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.main_activity_container, new MainFragment(), MAIN_FRAGMENT_TAG)
                                .commit();
                    }
                    FirebaseUtil.setProfilePicture(getApplicationContext(), mFirebaseAuth, viewHolder.headerImage);

                    viewHolder.usernameTextView.setText(mFirebaseAuth.getCurrentUser().getDisplayName());
                    viewHolder.emailTextView.setText(mFirebaseAuth.getCurrentUser().getEmail());

                } else {
                    GlideApp.with(getApplicationContext())
                            .load(R.drawable.user_icon)
                            .circleCrop()
                            .into(viewHolder.headerImage);

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_main_activity, menu);

        MenuItem menuItem = menu.findItem(R.id.searchMainActivity);
        viewHolder.searchView = (SearchView) menuItem.getActionView();

        viewHolder.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewHolder.searchListView.setVisibility(View.GONE);
                } else {
                    viewHolder.searchListView.setVisibility(View.VISIBLE);
                    mSearchAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        return true;
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
                //navigatetoPhotoActivity();
                openPhotoPicker();
                break;

            case R.id.menu_signout:
                FirebaseUtil.signOut(this);
                break;
        }
        viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openOptionsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_options, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
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
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void displayFragmentPlaceTypeList(Location location) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        MainFragment prevMain = (MainFragment) getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (prevMain.isVisible()) {
            ft.hide(prevMain);
        }

        PlaceTypeFragment placeTypeFragment = new PlaceTypeFragment();

        Bundle args = new Bundle();
        args.putParcelable(MainFragment.CURRENT_LOCATION_KEY, location);

        placeTypeFragment.setArguments(args);

        ft.add(R.id.main_activity_container, placeTypeFragment, PLACE_TYPE_FRAGMENT_TAG).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // signed in successfully

                // enable loading favorite places from Firebase
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(FavoriteFragment.LOAD_FIREBASE_KEY, FavoriteFragment.LOAD_FIREBASE_FIRST_TIME_LOGGED_IN);
                editor.commit();

            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            FirebaseUtil.uploadProfilePicture(getApplicationContext(), mFirebaseAuth, selectedImage, viewHolder.headerImage);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewHolder.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            MainFragment prevMain = (MainFragment) getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
            PlaceTypeFragment prevPlaceType = (PlaceTypeFragment) getFragmentManager().findFragmentByTag(PLACE_TYPE_FRAGMENT_TAG);

            if (prevPlaceType != null && !prevMain.isVisible()) {

                FragmentTransaction ft = getFragmentManager().beginTransaction();

                ft.remove(prevPlaceType);
                ft.show(prevMain);
                ft.commit();

            } else {
                super.onBackPressed();
            }
        }
    }
}
