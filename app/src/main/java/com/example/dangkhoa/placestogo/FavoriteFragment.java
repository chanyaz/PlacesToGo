package com.example.dangkhoa.placestogo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangkhoa.placestogo.RecyclerViewDecoration.FavoritePlaceItemSpacing;
import com.example.dangkhoa.placestogo.Utils.FirebaseUtil;
import com.example.dangkhoa.placestogo.Utils.NetworkUtil;
import com.example.dangkhoa.placestogo.Utils.SQLiteUtil;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.adapter.FavoritePlacesAdapter;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.database.DBContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutionException;

/**
 * Created by dangkhoa on 23/10/2017.
 */

public class FavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOAD_FIREBASE_KEY = "load once";
    public static final int LOAD_FIREBASE_FIRST_TIME_LOGGED_IN = 100;
    private static final int LOAD_ID = 1;

    private int loadFirebase;

    private FavoritePlacesAdapter adapter;
    private ViewHolder viewHolder;

    private LinearLayoutManager linearLayoutManager;

    private Paint paint;

    //**** Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mFavoritePlacesReference;
    private ChildEventListener mFavoriteEventListener;

    private NetworkChangeReceiver networkChangeReceiver;

    private SharedPreferences sharedPreferences;

    private Context mContext;

    private class ViewHolder {
        public SwipeRefreshLayout swipeRefreshLayout;
        public RecyclerView recyclerView;

        public ViewHolder(View view) {
            recyclerView = view.findViewById(R.id.favorite_recycler_view);
            swipeRefreshLayout = view.findViewById(R.id.favoriteSwipeRefreshLayout);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paint = new Paint();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        getContext().registerReceiver(networkChangeReceiver, intentFilter);

        mFirebaseAuth = FirebaseAuth.getInstance();
        // reference to user's favorite places in Firebase
        mFavoritePlacesReference = FirebaseDatabase.getInstance().getReference()
                .child(FirebaseUtil.USERS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(FirebaseUtil.FAVORITE_PLACES_CHILD);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        loadFirebase = sharedPreferences.getInt(LOAD_FIREBASE_KEY, 0);

        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        viewHolder = new ViewHolder(view);

        adapter = new FavoritePlacesAdapter(getContext(), null);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewHolder.recyclerView.setLayoutManager(linearLayoutManager);

        FavoritePlaceItemSpacing itemSpacing = new FavoritePlaceItemSpacing(5);
        viewHolder.recyclerView.addItemDecoration(itemSpacing);

        viewHolder.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String id = (String) viewHolder.itemView.getTag();
                // delete from sql local database
                getContext().getContentResolver().delete(DBContract.PlacesEntry.buildItemUri(id), null, null);

                // restart loader when item is deleted
                //getLoaderManager().restartLoader(LOAD_ID, null, FavoriteFragment.this);

                // notify Firebase about deletion
                // do not do this in listener because if user deletes the place when there is no internet connection, the sql database won't be notified
                // and the loader cannot work properly
                mFavoritePlacesReference.child(id).removeValue();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    paint.setColor(getContext().getColor(R.color.red));
                    RectF background;
                    RectF icon_dest;

                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);

                    if (dX > 0) {
                        background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                    } else {
                        background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                    }

                    c.drawRect(background, paint);
                    c.drawBitmap(icon, null, icon_dest, paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        }).attachToRecyclerView(viewHolder.recyclerView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.orange,
                    R.color.green, R.color.red);
        }
        viewHolder.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // do nothing
                viewHolder.swipeRefreshLayout.setRefreshing(false);
            }
        });


        viewHolder.swipeRefreshLayout.setRefreshing(true);

        if (loadFirebase == LOAD_FIREBASE_FIRST_TIME_LOGGED_IN) {
            getFavoritePlacesFromFirebase();
        } else {
            viewHolder.swipeRefreshLayout.setRefreshing(false);
        }
        attachFavoritePlacesListener();

        return view;
    }

    private void getFavoritePlacesFromFirebase() {
        if (NetworkUtil.checkInternetConnection(getContext())) {
            mFavoritePlacesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getChildrenCount() == 0) {
                        // set load firebase value to 0 --> Prevent this dialog from popping up when user launches this fragment the second, third, or nth time
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(LOAD_FIREBASE_KEY, 0);
                        editor.commit();

                        viewHolder.swipeRefreshLayout.setRefreshing(false);

                        return;
                    }

                    for (DataSnapshot favChild : dataSnapshot.getChildren()) {

                        final DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference()
                                .child(FirebaseUtil.PLACES_CHILD)
                                .child(favChild.getKey())
                                .child(FirebaseUtil.PLACE_DETAIL_CHILD);

                        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                PlaceDetail placeDetail = dataSnapshot.getValue(PlaceDetail.class);
                                // insert into SQLite Database
                                mContext.getContentResolver().insert(DBContract.PlacesEntry.CONTENT_URI, SQLiteUtil.valuesToDB(placeDetail));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        // set load firebase value to 0 --> Prevent this dialog from popping up when user launches this fragment the second, third, or nth time
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(LOAD_FIREBASE_KEY, 0);
                        editor.commit();

                        viewHolder.swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            viewHolder.swipeRefreshLayout.setRefreshing(false);

            // if user first logs in and there is no internet connection when they first launch FavoriteActivity
            // this alert dialog shows up to ask them whether they want to enable internet to load favorite places
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getContext().getString(R.string.firebase_favorite_load_no_internet_connection))
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NetworkUtil.setWifiState(getContext(), true);
                            viewHolder.swipeRefreshLayout.setRefreshing(true);

                            // set load firebase value to 0 --> Prevent this dialog from popping up when user launches this fragment the second, third, or nth time
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(LOAD_FIREBASE_KEY, 0);
                            editor.commit();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Register for Child Event Listener: notify when a new place is added to favorite or a favorite place is removed
     */
    private void attachFavoritePlacesListener() {

        if (mFavoriteEventListener == null) {

            if (NetworkUtil.checkInternetConnection(getContext())) {
                mFavoriteEventListener = new ChildEventListener() {
                    // this function should get called the first time the user logs in to load their favorite places from Firebase and store in sql database
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        final DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference()
                                .child(FirebaseUtil.PLACES_CHILD)
                                .child(dataSnapshot.getKey())
                                .child(FirebaseUtil.PLACE_DETAIL_CHILD);

                        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                PlaceDetail placeDetail = dataSnapshot.getValue(PlaceDetail.class);

                                Cursor cursor = mContext.getContentResolver().query(
                                        DBContract.PlacesEntry.buildItemUri(placeDetail.getId()),
                                        null,
                                        null,
                                        null,
                                        null);

                                if (cursor.getCount() == 0) {
                                    // insert into SQLite Database
                                    mContext.getContentResolver().insert(DBContract.PlacesEntry.CONTENT_URI, SQLiteUtil.valuesToDB(placeDetail));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        viewHolder.swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        // remove from sql local database
                        mContext.getContentResolver().delete(DBContract.PlacesEntry.buildItemUri(dataSnapshot.getKey()), null, null);

                        viewHolder.swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mFavoritePlacesReference.addChildEventListener(mFavoriteEventListener);

            } else {
                viewHolder.swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    /**
     * Unregister Child Event Listener
     */
    private void detachFavoritePlacesListener() {
        if (mFavoriteEventListener != null) {
            mFavoritePlacesReference.removeEventListener(mFavoriteEventListener);
            mFavoriteEventListener = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        detachFavoritePlacesListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOAD_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getContext(), DBContract.PlacesEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            int status = NetworkUtil.getConnectivityStatusString(context);

            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

            } else {
                if (loadFirebase == LOAD_FIREBASE_FIRST_TIME_LOGGED_IN) {
                    getFavoritePlacesFromFirebase();
                }
                attachFavoritePlacesListener();
            }
        }
    }
}
