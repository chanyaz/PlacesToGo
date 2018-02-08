package com.example.dangkhoa.placestogo;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.dangkhoa.placestogo.adapter.GooglePlacesAutoCompleteAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainFragment.MorePlacesCallback {

    private static final String LOG_TAG = "MAIN";
    private static final int RC_SIGN_IN = 100;

    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private static final String PLACE_TYPE_FRAGMENT_TAG = "place_type_fragment_tag";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private DatabaseReference mFavoritePlacesReference;
    private ChildEventListener mChildEventListener;

    private class ViewHolder {

        public DrawerLayout drawerLayout;
        public NavigationView navigationView;
        public ActionBarDrawerToggle toggle;
        public Toolbar toolbar;

        public ListView searchListView;

        public ViewHolder() {
            toolbar = findViewById(R.id.main_toolbar);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            searchListView = findViewById(R.id.searchListView);
        }
    }

    private ViewHolder viewHolder;

    private GooglePlacesAutoCompleteAdapter mSearchAdapter;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mFirebaseAuth = FirebaseAuth.getInstance();

        viewHolder = new ViewHolder();

        setSupportActionBar(viewHolder.toolbar);

        mSearchAdapter = new GooglePlacesAutoCompleteAdapter(this);
        viewHolder.searchListView.setAdapter(mSearchAdapter);

        viewHolder.toggle = new ActionBarDrawerToggle(this, viewHolder.drawerLayout, viewHolder.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        viewHolder.drawerLayout.addDrawerListener(viewHolder.toggle);
        viewHolder.toggle.syncState();

        viewHolder.navigationView.setNavigationItemSelectedListener(this);

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

                } else {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // signed in successfully
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_main_activity, menu);

        MenuItem menuItem = menu.findItem(R.id.searchMainActivity);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                break;
            case R.id.menu_signout:
                Util.signOut(this);
                break;
        }
        viewHolder.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
