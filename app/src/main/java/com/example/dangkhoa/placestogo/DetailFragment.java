package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangkhoa.placestogo.adapter.GooglePlacesAutoCompleteAdapter;
import com.example.dangkhoa.placestogo.adapter.PlaceListAdapter;
import com.example.dangkhoa.placestogo.adapter.ReviewListAdapter;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.data.Review;
import com.example.dangkhoa.placestogo.database.DBContract;
import com.example.dangkhoa.placestogo.service.PlaceDetailService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 01/10/2017.
 */

public class DetailFragment extends Fragment {

    public static final String PLACE_ARG_BUNDLE_KEY = "place_arg_bundle_key";
    public static final String FLAG_ARG_BUNDLE_KEY = "flag_arg_bundle_key";

    private static final String PLACE_SAVE_KEY = "place_save_key";

    private boolean mIsInDatabase;

    public DetailFragment() {

    }

    private PlaceDetail mPlaceDetail;
    private ArrayList<Review> mReviewList;
    private int mFlagActivity;

    private boolean isServiceFinished = false;

    private PlaceDetailServiceReceiver receiver;

    private ReviewListAdapter mAdapter;
    private LinearLayoutManager mLinearlayoutManager;

    private ViewHolder viewHolder;

    private class ViewHolder {

        public CollapsingToolbarLayout collapsingToolbarLayout;
        public ImageView thumbnailImage, backdropImage;
        public TextView nameText, addressText, phoneText, openingText;
        public ImageButton phoneButton, locationButton, websiteButton, uberButton, shareButton;
        public RatingBar ratingBar;
        public RecyclerView reviewRecyclerView;
        public ProgressBar progressBar;
        public FloatingActionButton likeButton;

        public ViewHolder(View view) {
            thumbnailImage = view.findViewById(R.id.placeThumbnailImage);
            backdropImage = view.findViewById(R.id.place_back_drop_imageView);

            nameText = view.findViewById(R.id.place_name_textView);
            openingText = view.findViewById(R.id.opening_textView);
            addressText = view.findViewById(R.id.place_address_textView);
            phoneText = view.findViewById(R.id.place_phone_textView);

            phoneButton = view.findViewById(R.id.phoneButton);
            locationButton = view.findViewById(R.id.locationButton);
            websiteButton = view.findViewById(R.id.websiteButton);
            uberButton = view.findViewById(R.id.uberButton);
            shareButton = view.findViewById(R.id.shareButton);

            ratingBar = view.findViewById(R.id.detail_ratingBar);

            reviewRecyclerView = view.findViewById(R.id.review_recyclerview);

            progressBar = view.findViewById(R.id.progressBar);

            collapsingToolbarLayout = view.findViewById(R.id.detail_fragment_toolbar_layout);

            likeButton = view.findViewById(R.id.detail_fragment_favorite_button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // to make sure the progress bar is not visible accidentally when service has finished
        if (isServiceFinished) {
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (isServiceFinished) {
            outState.putParcelable(PLACE_SAVE_KEY, mPlaceDetail);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter(PlaceDetailServiceReceiver.PLACE_DETAIL_RECEIVER);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new PlaceDetailServiceReceiver();
        getContext().registerReceiver(receiver, intentFilter);

        if (savedInstanceState != null && savedInstanceState.containsKey(PLACE_SAVE_KEY)) {
            mPlaceDetail = savedInstanceState.getParcelable(PLACE_SAVE_KEY);
        } else {
            Bundle bundle = getArguments();
            mPlaceDetail = bundle.getParcelable(PLACE_ARG_BUNDLE_KEY);
            mFlagActivity = bundle.getInt(FLAG_ARG_BUNDLE_KEY);
        }
        mReviewList = new ArrayList<>();
        mAdapter = new ReviewListAdapter(getActivity(), mReviewList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        viewHolder = new ViewHolder(view);

        DatabaseQuery databaseQuery = new DatabaseQuery();
        databaseQuery.execute();

        mLinearlayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewHolder.reviewRecyclerView.setLayoutManager(mLinearlayoutManager);

        viewHolder.phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mPlaceDetail.getInternationalPhone();
                if (phone != null) {
                    Util.createCallIntent(getContext(), mPlaceDetail.getInternationalPhone());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getContext().getResources().getString(R.string.phone_is_not_available_message))
                            .setNegativeButton(getContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        viewHolder.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.createMapIntent(getContext(), mPlaceDetail.getLatitude(), mPlaceDetail.getLongitude(), mPlaceDetail.getName());
            }
        });

        viewHolder.websiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaceDetail.getWebsite() != null) {
                    Intent menuWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPlaceDetail.getWebsite()));
                    if (menuWebIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(menuWebIntent);
                    }
                } else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.website_is_not_available_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.uberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.requestUber(getContext(), mPlaceDetail.getLatitude(), mPlaceDetail.getLongitude(), mPlaceDetail.getName());
            }
        });

        viewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mPlaceDetail.getName() + " " + getContext().getResources().getString(R.string.place_share);
                Util.shareIntent(getContext(), message);
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(PLACE_SAVE_KEY)) {
            ArrayList<Review> list = mPlaceDetail.getReviews();

            isServiceFinished = true;

            updateUI();
            populateReviewList(list);

        } else {
            // if this fragment is called from list activity or by clicking on search view
            // we don't need to do extra things except calling refresh
            if (mFlagActivity == GooglePlacesAutoCompleteAdapter.FLAG_SEARCH_LIST_ADAPTER || mFlagActivity == PlaceListAdapter.FLAG_PLACE_LIST_ADAPTER) {
                // do nothing here

                // if this fragment is called from favorite activity
                // then we update UI first before calling refresh to get update with new reviews
            } else {
                updateUI();
            }
            refresh();
        }

        viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsInDatabase) {
                    getContext().getContentResolver().insert(DBContract.PlacesEntry.CONTENT_URI, valuesToDB(mPlaceDetail));
                    viewHolder.likeButton.setImageResource(R.drawable.ic_favorite_pressed);
                    mIsInDatabase = true;
                } else {
                    getContext().getContentResolver().delete(DBContract.PlacesEntry.buildItemUri(mPlaceDetail.getId()), null, null);
                    viewHolder.likeButton.setImageResource(R.drawable.ic_favorite);
                    mIsInDatabase = false;
                }

            }
        });

        return view;
    }

    /*
        This AsyncTask class will query database in the background to check whether the current place is in database
        If it is, set the image source of the like button to pressed state.
     */
    private class DatabaseQuery extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            Cursor cursor = null;

            try {
                cursor = getContext().getContentResolver().query(
                        DBContract.PlacesEntry.buildItemUri(mPlaceDetail.getId()),
                        null,
                        null,
                        null,
                        null);

            } catch (Exception e) {

            } finally {
                cursor.close();
            }

            if (cursor.getCount() == 1) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isInDatabase) {
            super.onPostExecute(isInDatabase);

            if (isInDatabase) {
                mIsInDatabase = true;
                viewHolder.likeButton.setImageResource(R.drawable.ic_favorite_pressed);
            } else {
                mIsInDatabase = false;
            }
        }
    }

    public class PlaceDetailServiceReceiver extends BroadcastReceiver {
        public static final String PLACE_DETAIL_RECEIVER = "com.example.android.placestogo.placeDetail";

        @Override
        public void onReceive(Context context, Intent intent) {
            isServiceFinished = true;

            mPlaceDetail = intent.getParcelableExtra(PlaceDetailService.PLACE_DETAIL_RESPONSE_KEY);
            ArrayList<Review> list = mPlaceDetail.getReviews();

            // we need to update UI if this fragment is called by list activity or from search view
            if (mFlagActivity == GooglePlacesAutoCompleteAdapter.FLAG_SEARCH_LIST_ADAPTER || mFlagActivity == PlaceListAdapter.FLAG_PLACE_LIST_ADAPTER) {
                updateUI();
            }
            populateReviewList(list);
        }
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void populateReviewList(ArrayList<Review> list) {
        mReviewList.addAll(list);

        mAdapter.notifyItemRangeInserted(0, list.size());
        viewHolder.reviewRecyclerView.setAdapter(mAdapter);

        viewHolder.progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateUI() {
        viewHolder.collapsingToolbarLayout.setTitle(mPlaceDetail.getName());
        viewHolder.collapsingToolbarLayout.setCollapsedTitleTextColor(getContext().getColor(R.color.white));
        viewHolder.collapsingToolbarLayout.setExpandedTitleColor(getContext().getColor(android.R.color.transparent));

        viewHolder.nameText.setText(mPlaceDetail.getName());
        viewHolder.addressText.setText(mPlaceDetail.getAddress());
        viewHolder.phoneText.setText(mPlaceDetail.getInternationalPhone());

        if (mPlaceDetail.getOpening() == 1) {
            viewHolder.openingText.setText(getContext().getResources().getString(R.string.open_now));
            viewHolder.openingText.setTextColor(getContext().getColor(R.color.green));
        } else {
            viewHolder.openingText.setText(getContext().getResources().getString(R.string.closed_now));
            viewHolder.openingText.setTextColor(getContext().getColor(R.color.red));
        }

        if (mPlaceDetail.getImage_url() != null && !mPlaceDetail.getImage_url().equals("")) {
            Picasso.with(getContext())
                    .load(mPlaceDetail.getImage_url())
                    .error(R.mipmap.ic_launcher)
                    .into(viewHolder.thumbnailImage);

            Picasso.with(getContext())
                    .load(mPlaceDetail.getImage_url())
                    .error(R.mipmap.ic_launcher)
                    .into(viewHolder.backdropImage);
        } else {
            Picasso.with(getContext())
                    .load(R.mipmap.ic_launcher)
                    .into(viewHolder.thumbnailImage);

            Picasso.with(getContext())
                    .load(R.mipmap.ic_launcher)
                    .into(viewHolder.backdropImage);
        }
        viewHolder.ratingBar.setRating((float) mPlaceDetail.getRating());
    }

    private void refresh() {
        if (!Util.checkInternetConnection(getContext())) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
        } else {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            isServiceFinished = false;

            Intent intent = new Intent(getActivity(), PlaceDetailService.class);
            intent.putExtra(PlaceDetailService.PLACE_ID_KEY, mPlaceDetail);
            getContext().startService(intent);
        }
    }

    private ContentValues valuesToDB(PlaceDetail placeDetail) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBContract.PlacesEntry.COLUMN_PLACE_ID, placeDetail.getId());
        contentValues.put(DBContract.PlacesEntry.COLUMN_NAME, placeDetail.getName());
        contentValues.put(DBContract.PlacesEntry.COLUMN_ADDRESS, placeDetail.getAddress());
        contentValues.put(DBContract.PlacesEntry.COLUMN_IMAGE_URL, placeDetail.getImage_url());
        contentValues.put(DBContract.PlacesEntry.COLUMN_LATITUDE, String.valueOf(placeDetail.getLatitude()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_LONGITUDE, String.valueOf(placeDetail.getLongitude()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_RATING, String.valueOf(placeDetail.getRating()));
        contentValues.put(DBContract.PlacesEntry.COLUMN_LOCALITY, placeDetail.getLocality());
        contentValues.put(DBContract.PlacesEntry.COLUMN_COUNTRY, placeDetail.getCountry());
        contentValues.put(DBContract.PlacesEntry.COLUMN_POSTCODE, placeDetail.getPostCode());
        contentValues.put(DBContract.PlacesEntry.COLUMN_WEBSITE, placeDetail.getWebsite());
        contentValues.put(DBContract.PlacesEntry.COLUMN_PHONE, placeDetail.getInternationalPhone());

        return contentValues;
    }
}
