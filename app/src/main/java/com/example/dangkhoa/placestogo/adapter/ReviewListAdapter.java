package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Util;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.data.Review;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 01/10/2017.
 */

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Review> mList;

    public ReviewListAdapter(Context context, ArrayList<Review> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.review_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Review review = mList.get(position);

        viewHolder.userName.setText(review.getUser_name());
        viewHolder.reviewText.setText(review.getReview_text());

        // if the time received cannot be converted to a date --> this time is from google
        if (!Util.isValidDate(review.getReview_time_friendly())) {
            viewHolder.reviewTime.setText(review.getReview_time_friendly());

        } else {
            viewHolder.reviewTime.setText(Util.timeFriendly(mContext, review.getReview_time_friendly()));
        }

        viewHolder.rating.setRating(Float.parseFloat(review.getUser_rating()));

        String profile_image_url = review.getProfile_image_url();

        if (profile_image_url != null && !profile_image_url.equals("")) {
            Picasso.with(mContext)
                    .load(profile_image_url)
                    .into(viewHolder.user_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) viewHolder.user_image.getDrawable()).getBitmap();
                            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
                            roundedBitmapDrawable.setCircular(true);
                            roundedBitmapDrawable.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                            viewHolder.user_image.setImageDrawable(roundedBitmapDrawable);
                        }

                        @Override
                        public void onError() {
                            viewHolder.user_image.setImageResource(R.mipmap.ic_launcher);
                        }
                    });
        } else {
            Picasso.with(mContext)
                    .load(R.mipmap.ic_launcher)
                    .into(viewHolder.user_image);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView user_image;
        public TextView userName, reviewText, reviewTime;
        public RatingBar rating;

        public ViewHolder(View view) {
            super(view);

            user_image = view.findViewById(R.id.user_image_profile);
            userName = view.findViewById(R.id.user_name);
            reviewText = view.findViewById(R.id.review_text);
            reviewTime = view.findViewById(R.id.review_time);
            rating = view.findViewById(R.id.user_rating_bar);
        }
    }
}
