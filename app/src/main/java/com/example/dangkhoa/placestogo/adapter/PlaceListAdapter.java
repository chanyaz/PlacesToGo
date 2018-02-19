package com.example.dangkhoa.placestogo.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.DetailActivity;
import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 29/09/2017.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int FLAG_PLACE_LIST_ADAPTER = 200;

    private Context mContext;
    private ArrayList<PlaceDetail> mList;

    private Location mLocation;
    private String unit;

    public PlaceListAdapter(Context context, ArrayList<PlaceDetail> list, Location location) {
        mContext = context;
        mList = list;
        mLocation = location;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        getSharedPreferences();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView thumbnail;
        public TextView resName, resAddress, resOpening, distanceTextView;
        public RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.featured_image);
            resName = itemView.findViewById(R.id.restaurant_name);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            resAddress = itemView.findViewById(R.id.item_locality);
            resOpening = itemView.findViewById(R.id.opening_text);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailActivity.PLACE_BUNDLE_KEY, mList.get(position));
            bundle.putInt(DetailActivity.FLAG_KEY, FLAG_PLACE_LIST_ADAPTER);

            intent.putExtra(DetailActivity.INTENT_PACKAGE, bundle);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, thumbnail, thumbnail.getTransitionName());

                ((Activity) mContext).getWindow().setSharedElementEnterTransition(TransitionInflater.from(mContext).inflateTransition(R.transition.curve));
                mContext.startActivity(intent, options.toBundle());
            } else {
                mContext.startActivity(intent);
            }

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.list_item_place, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        PlaceDetail placeDetail = mList.get(position);

        if (placeDetail.getImage_url() != null && !placeDetail.getImage_url().equals("")) {
            GlideApp.with(mContext)
                    .load(placeDetail.getImage_url())
                    .centerCrop()
                    .into(viewHolder.thumbnail);
        } else {
            GlideApp.with(mContext)
                    .load(R.mipmap.ic_launcher)
                    .fitCenter()
                    .into(viewHolder.thumbnail);
        }
        viewHolder.resName.setText(placeDetail.getName());
        viewHolder.resAddress.setText(placeDetail.getAddress());
        viewHolder.ratingBar.setRating((float) placeDetail.getRating());

        if (placeDetail.getOpening() == 1) {
            viewHolder.resOpening.setTextColor(mContext.getColor(R.color.green));
            viewHolder.resOpening.setText(mContext.getResources().getString(R.string.open_now));
        } else {
            viewHolder.resOpening.setTextColor(mContext.getColor(R.color.red));
            viewHolder.resOpening.setText(mContext.getResources().getString(R.string.closed_now));
        }

        String distanceText = Util.convertMeterTo(mContext, unit, Float.parseFloat(placeDetail.getDistance()));
        viewHolder.distanceTextView.setText(distanceText);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        getSharedPreferences();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void getSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        unit = sharedPreferences.getString(mContext.getString(R.string.pref_unit_key), mContext.getString(R.string.pref_unit_kilometers_value));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
}
