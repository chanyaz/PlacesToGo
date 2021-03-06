package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.dangkhoa.placestogo.RecyclerViewDecoration.PlaceListItemSpacing;
import com.example.dangkhoa.placestogo.Utils.NetworkUtil;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.adapter.PlaceListAdapter;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.service.PlaceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dangkhoa on 29/09/2017.
 */

public class PlaceListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public PlaceListFragment() {

    }

    public static final String LOCATION_BUNDLE_KEY = "location_bundle_key";
    public static final String PLACE_TYPE_BUNDLE_KEY = "place_type_bundle_key";

    private static final String PLACE_LIST_SAVE_KEY = "place_list_save_key";
    private static final String RECYCLER_VIEW_STATE_SAVE_KEY = "recycler_view_state_save_key";
    private static final String NEXT_PAGE_TOKEN_SAVE_KEY = "next_page_token_save_key";
    private static final String SORT_MODE_SAVE_KEY = "sort_mode_save_key";

    private static final int SORT_BY_DISTANCE = 0;
    private static final int SORT_BY_OPENING = 1;
    private static final int SORT_BY_ALPHABET = 2;
    private static final int SORT_BY_RATING = 3;

    private int SORT_MODE = -1;

    private boolean requestLocationOnNetworkReconnected = false;

    private float RADIUS;
    private String UNIT;

    private ArrayList<PlaceDetail> mPlaceList;

    private Location mLastLocation;
    private String mPlaceType;

    private int mPositionToScrollTo = 0;
    private String mNextPageToken;

    private boolean mIsRefreshing = false;
    private boolean placesAreBeingLoaded = false;
    private boolean loadMorePlaces = false;

    // used this variable to check whether the service has finished or not
    // if the service has not finished (due to slow connection) and user suddenly rotates the screen
    // therefore, no information can be used to save in OnSaveInstance
    // we use this variable to make sure that we don't save null information
    private boolean isServiceFinished = false;

    private boolean hasSavedState;
    private Parcelable recyclerViewState;

    private GridLayoutManager gridLayoutManager;

    private PlaceServiceReceiver receiver;
    private NetworkChangeReceiver networkChangeReceiver;

    private PlaceListAdapter placeListAdapter;

    private ViewHolder viewHolder;

    public interface PlacesCallback {
        void radiusAchieved(float radius);

        void placesListAchieved(ArrayList<PlaceDetail> list);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        RADIUS = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_3_0_value)));
        UNIT = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_kilometers_value));

        resetOffsetAndNextPageToken();
        refresh();
        //refreshMock();
    }

    private class ViewHolder {

        public SwipeRefreshLayout mSwipeRefreshLayout;
        public RecyclerView mRecyclerView;
        public Spinner sortSpinner;

        public ViewHolder(View view) {
            mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            mRecyclerView = view.findViewById(R.id.recyclerView);
            sortSpinner = view.findViewById(R.id.sortSpinner);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (isServiceFinished) {
            outState.putParcelableArrayList(PLACE_LIST_SAVE_KEY, mPlaceList);
            outState.putParcelable(RECYCLER_VIEW_STATE_SAVE_KEY, viewHolder.mRecyclerView.getLayoutManager().onSaveInstanceState());
            outState.putString(NEXT_PAGE_TOKEN_SAVE_KEY, mNextPageToken);
            outState.putInt(SORT_MODE_SAVE_KEY, SORT_MODE);
        }

        // these information is retrieved from main activity
        // we can get them before the service starts
        // so, we have to save them
        outState.putParcelable(LOCATION_BUNDLE_KEY, mLastLocation);
        outState.putString(PLACE_TYPE_BUNDLE_KEY, mPlaceType);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilterAddress = new IntentFilter(PlaceServiceReceiver.PLACE_LIST_RECEIVER);
        intentFilterAddress.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new PlaceListFragment.PlaceServiceReceiver();
        getContext().registerReceiver(receiver, intentFilterAddress);

        IntentFilter intentFilterNetwork = new IntentFilter();
        intentFilterNetwork.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        getContext().registerReceiver(networkChangeReceiver, intentFilterNetwork);

        if (savedInstanceState != null) {
            mLastLocation = savedInstanceState.getParcelable(LOCATION_BUNDLE_KEY);
            mPlaceType = savedInstanceState.getString(PLACE_TYPE_BUNDLE_KEY);
            mNextPageToken = savedInstanceState.getString(NEXT_PAGE_TOKEN_SAVE_KEY);
            SORT_MODE = savedInstanceState.getInt(SORT_MODE_SAVE_KEY);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mLastLocation = arguments.getParcelable(LOCATION_BUNDLE_KEY);
                mPlaceType = arguments.getString(PLACE_TYPE_BUNDLE_KEY);
            }
        }
        mPlaceList = new ArrayList<>();
        placeListAdapter = new PlaceListAdapter(getContext(), mPlaceList, mLastLocation);
    }

    /**
     * Get shared preferences
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        RADIUS = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_3_0_value)));
        UNIT = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_kilometers_value));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.list_fragment_place, container, false);

        viewHolder = new ViewHolder(view);

        gridLayoutManager = new GridLayoutManager(getContext(), 2, StaggeredGridLayoutManager.VERTICAL, false);
        viewHolder.mRecyclerView.setLayoutManager(gridLayoutManager);

        PlaceListItemSpacing itemSpacing = new PlaceListItemSpacing(14);
        viewHolder.mRecyclerView.addItemDecoration(itemSpacing);

        setupSharedPreferences();

        viewHolder.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItem = gridLayoutManager.findLastCompletelyVisibleItemPosition();

                if (!placesAreBeingLoaded && lastVisibleItem >= totalItemCount - 1) {
                    mPositionToScrollTo = totalItemCount;

                    if (mNextPageToken != null) {
                        loadMorePlaces = true;
                        placesAreBeingLoaded = true;
                        refresh();
                        //refreshMock();
                    } /*else {
                        loadMorePlaces = true;
                        placesAreBeingLoaded = true;
                        refreshMock();
                    } */
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.mSwipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.orange,
                    R.color.green, R.color.red);
        }
        viewHolder.mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetOffsetAndNextPageToken();
                refresh();
                //refreshMock();
            }
        });

        setupSpinner();

        // only get saved list when the list was saved
        // if it was not saved because the service had not finished
        // we'll start service again
        if (savedInstanceState != null && savedInstanceState.containsKey(PLACE_LIST_SAVE_KEY)) {
            // get saved list
            ArrayList<PlaceDetail> list = savedInstanceState.getParcelableArrayList(PLACE_LIST_SAVE_KEY);

            // when the list is saved, we need to set this variable to true
            // because in the new state, if we don't set this variable to true here, its value is false at the beginning of new state
            // so, when we rotate the screen, this variable is still false
            // and this app won't save our list
            isServiceFinished = true;

            hasSavedState = true;
            recyclerViewState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE_SAVE_KEY);

            updateUI(list);
        } else {
            refresh();
            //refreshMock();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(receiver);
        getContext().unregisterReceiver(networkChangeReceiver);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Reset next page token and position to scroll to in recycler view
     */
    private void resetOffsetAndNextPageToken() {
        mPositionToScrollTo = 0;
        mNextPageToken = null;
    }

    private void refresh() {
        if (!NetworkUtil.checkInternetConnection(getContext())) {
            mIsRefreshing = false;
            viewHolder.mSwipeRefreshLayout.setRefreshing(mIsRefreshing);

            // make sure that when internet connection is reconnected, it will get more items if available
            if (loadMorePlaces) {
                loadMorePlaces = false;
                placesAreBeingLoaded = false;
            }

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setMessage(getContext().getString(R.string.places_load_no_internet_connection))
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // make sure that when internet connection is reconnected, it will get more items if available
                            if (loadMorePlaces) {
                                loadMorePlaces = false;
                                placesAreBeingLoaded = false;
                            }

                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // turn on wifi
                            NetworkUtil.setWifiState(getContext(), true);
                            requestLocationOnNetworkReconnected = true;

                            loadMorePlaces = true;
                            placesAreBeingLoaded = true;

                            viewHolder.mSwipeRefreshLayout.setRefreshing(true);
                        }
                    });

            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            mIsRefreshing = true;
            viewHolder.mSwipeRefreshLayout.setRefreshing(mIsRefreshing);

            // when service starts, we set this variable to false
            // in case the service has not finished and user rotates screen
            // we won't save any null information
            isServiceFinished = false;

            Intent intent = new Intent(getContext(), PlaceService.class);
            intent.putExtra(PlaceService.CURRENT_LOCATION_KEY, mLastLocation);
            intent.putExtra(PlaceService.PLACE_TYPE_KEY, mPlaceType);
            intent.putExtra(PlaceService.RADIUS_KEY, Util.radiusInMeter(getContext(), RADIUS, UNIT));
            intent.putExtra(PlaceService.NEXT_PAGE_TOKEN_KEY, mNextPageToken);
            getContext().startService(intent);
        }
    }

    /**
     * Update RecyclerView with the place list
     *
     * @param results
     */
    private void updateUI(ArrayList<PlaceDetail> results) {
        mIsRefreshing = false;
        viewHolder.mSwipeRefreshLayout.setRefreshing(mIsRefreshing);

        int currentSize;
        // if swipe refresh layout is fired - not in load more places mode
        // then, clear the place list with old data set
        if (!loadMorePlaces) {
            // capture the number of old items in the list before clearing
            currentSize = mPlaceList.size();
            // clear the list
            mPlaceList.clear();
            // notify the recycler view adapter the number items deleted
            // in this case, start from the 0th position to the very end position
            placeListAdapter.notifyItemRangeRemoved(0, currentSize);
        }
        // if load more places mode is fired, simply add the new list items to the current list
        // but before adding, we need to capture the number of items of the current list
        currentSize = mPlaceList.size();
        // then, add the new list into the current list
        mPlaceList.addAll(results);
        // notify the adapter the number of new items we have added
        // starting from the very last item of the adapter list
        placeListAdapter.notifyItemRangeInserted(currentSize, results.size());

        switch (SORT_MODE) {
            case SORT_BY_DISTANCE:
                sortByDistance();
                break;
            case SORT_BY_OPENING:
                sortByOpening();
                break;
            case SORT_BY_ALPHABET:
                sortByAlphabet();
                break;
            case SORT_BY_RATING:
                sortByRating();
                break;
            default:
                // do nothing
        }
        viewHolder.mRecyclerView.setAdapter(placeListAdapter);

        if (hasSavedState) {
            viewHolder.mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }

        loadMorePlaces = false;
        placesAreBeingLoaded = false;
        hasSavedState = false;

        viewHolder.mRecyclerView.scrollToPosition(mPositionToScrollTo);
        ((PlacesCallback) getContext()).radiusAchieved(Float.parseFloat(Util.radiusInMeter(getContext(), RADIUS, UNIT)));
        ((PlacesCallback) getContext()).placesListAchieved(mPlaceList);
    }

    /**
     * Setup Spinner for sorting
     */
    private void setupSpinner() {
        String[] sortArrays = {
                getContext().getString(R.string.distance),
                getContext().getString(R.string.opening),
                getContext().getString(R.string.alphabet),
                getContext().getString(R.string.rate)
        };

        ArrayAdapter spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, sortArrays);
        viewHolder.sortSpinner.setAdapter(spinnerAdapter);

        viewHolder.sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case SORT_BY_DISTANCE:
                        SORT_MODE = SORT_BY_DISTANCE;
                        sortByDistance();
                        break;
                    case SORT_BY_OPENING:
                        SORT_MODE = SORT_BY_OPENING;
                        sortByOpening();
                        break;
                    case SORT_BY_ALPHABET:
                        SORT_MODE = SORT_BY_ALPHABET;
                        sortByAlphabet();
                        break;
                    case SORT_BY_RATING:
                        SORT_MODE = SORT_BY_RATING;
                        sortByRating();
                        break;
                    default:
                        // do nothing
                }
                placeListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Sort by opening: places opening are placed on top
     */
    private void sortByOpening() {
        Collections.sort(mPlaceList, new Comparator<PlaceDetail>() {
            @Override
            public int compare(PlaceDetail placeDetail1, PlaceDetail placeDetail2) {
                if (placeDetail1.getOpening() >= placeDetail2.getOpening()) {
                    return -1;
                }
                if (placeDetail1.getOpening() < placeDetail2.getOpening()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    /**
     * Sort by distance: places nearest the current user's location are placed on top
     */
    private void sortByDistance() {
        Collections.sort(mPlaceList, new Comparator<PlaceDetail>() {
            @Override
            public int compare(PlaceDetail placeDetail1, PlaceDetail placeDetail2) {
                if (Float.parseFloat(placeDetail1.getDistance()) < Float.parseFloat(placeDetail2.getDistance())) {
                    return -1;
                }
                if (Float.parseFloat(placeDetail1.getDistance()) >= Float.parseFloat(placeDetail2.getDistance())) {
                    return 1;
                }
                return 0;
            }
        });
    }

    /**
     * Sort by alphabet: places are placed in alphabet order
     */
    private void sortByAlphabet() {
        Collections.sort(mPlaceList, new Comparator<PlaceDetail>() {
            @Override
            public int compare(PlaceDetail placeDetail1, PlaceDetail placeDetail2) {
                return placeDetail1.getName().compareTo(placeDetail2.getName());
            }
        });
    }

    /**
     * Sort by rating: places with higher rating are placed on top
     */
    private void sortByRating() {
        Collections.sort(mPlaceList, new Comparator<PlaceDetail>() {
            @Override
            public int compare(PlaceDetail placeDetail1, PlaceDetail placeDetail2) {
                if (placeDetail1.getRating() >= placeDetail2.getRating()) {
                    return -1;
                }
                if (placeDetail1.getRating() < placeDetail2.getRating()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public class PlaceServiceReceiver extends BroadcastReceiver {

        public static final String PLACE_LIST_RECEIVER = "com.example.android.placestogo.placelistservice";

        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceFinished = true;

            mNextPageToken = intent.getStringExtra(PlaceService.NEXT_PAGE_TOKEN_KEY);
            ArrayList<PlaceDetail> results = intent.getParcelableArrayListExtra(PlaceService.PLACE_LIST_KEY_RESPONSE);

            updateUI(results);
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            int status = NetworkUtil.getConnectivityStatusString(context);

            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

            } else {
                if (requestLocationOnNetworkReconnected) {
                    refresh();
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // TESTING FUNCTIONS

    /*
        Mock list generation and refresh mock function are used for testing ---------------
     */

    private ArrayList<PlaceDetail> mockList() {
        ArrayList<PlaceDetail> list = new ArrayList<>();

        PlaceDetail placeDetail1 = new PlaceDetail("id1", "name1", "address1", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail2 = new PlaceDetail("id2", "name2", "address2", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail3 = new PlaceDetail("id3", "name3", "address3", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail4 = new PlaceDetail("id4", "name4", "address4", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail5 = new PlaceDetail("id5", "name5", "address5", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail6 = new PlaceDetail("id6", "name6", "address6", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail7 = new PlaceDetail("id7", "name7", "address7", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail8 = new PlaceDetail("id8", "name8", "address8", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail9 = new PlaceDetail("id9", "name9", "address9", "image_url1", 1.00, 1.00, 1, 1, null);
        PlaceDetail placeDetail10 = new PlaceDetail("id10", "name10", "address10", "image_url1", 1.00, 1.00, 1, 1, null);

        list.add(placeDetail1);
        list.add(placeDetail2);
        list.add(placeDetail3);
        list.add(placeDetail4);
        list.add(placeDetail5);
        list.add(placeDetail6);
        list.add(placeDetail7);
        list.add(placeDetail8);
        list.add(placeDetail9);
        list.add(placeDetail10);

        return list;
    }

    private void refreshMock() {
        isServiceFinished = false;
        updateUI(mockList());
        isServiceFinished = true;
    }
}
