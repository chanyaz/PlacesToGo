package com.example.dangkhoa.placestogo.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.DetailActivity;
import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Utils.SQLiteUtil;
import com.example.dangkhoa.placestogo.database.DBContract;

/**
 * Created by dangkhoa on 23/10/2017.
 */

public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder> {

    private static final int FLAG_FAVORITE_PLACE_LIST_ADAPTER = 300;

    private Context mContext;
    private Cursor mCursor;

    public FavoritePlacesAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_favorite, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        String id = mCursor.getString(mCursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_PLACE_ID));
        holder.itemView.setTag(id);

        String name = mCursor.getString(mCursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_NAME));
        holder.placeName.setText(name);

        String thumbnail = mCursor.getString(mCursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_IMAGE_URL));
        if (thumbnail != null && !thumbnail.equals("")) {
            GlideApp.with(mContext)
                    .load(thumbnail)
                    .into(holder.thumbnail);
        } else {
            GlideApp.with(mContext)
                    .load(R.mipmap.ic_launcher)
                    .into(holder.thumbnail);
        }

        String rating = mCursor.getString(mCursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_RATING));
        holder.ratingBar.setRating(Float.parseFloat(rating));

        String locality = mCursor.getString(mCursor.getColumnIndex(DBContract.PlacesEntry.COLUMN_LOCALITY));
        holder.placeLocality.setText(locality);

    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView thumbnail;
        public TextView placeName, placeLocality;
        public RatingBar ratingBar;

        public ViewHolder(View view) {
            super(view);

            thumbnail = view.findViewById(R.id.favorite_featured_image);
            placeName = view.findViewById(R.id.favorite_place_name);
            ratingBar = view.findViewById(R.id.favorite_ratingBar);
            placeLocality = view.findViewById(R.id.favorite_place_locality);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);

            Intent intent = new Intent(mContext, DetailActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailActivity.PLACE_BUNDLE_KEY, SQLiteUtil.cursorPlace(mCursor));
            bundle.putInt(DetailActivity.FLAG_KEY, FLAG_FAVORITE_PLACE_LIST_ADAPTER);

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
}
