package com.example.dangkhoa.placestogo.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class Review implements Parcelable {
    private String user_name;
    private String review_text;
    private String user_rating;
    private String review_time_friendly;
    private String profile_image_url;

    public Review() {

    }

    protected Review(Parcel in) {
        user_name = in.readString();
        review_text = in.readString();
        user_rating = in.readString();
        review_time_friendly = in.readString();
        profile_image_url = in.readString();
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }

    public String getUser_rating() {
        return user_rating;
    }

    public void setUser_rating(String user_rating) {
        this.user_rating = user_rating;
    }

    public String getReview_time_friendly() {
        return review_time_friendly;
    }

    public void setReview_time_friendly(String review_time_friendly) {
        this.review_time_friendly = review_time_friendly;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_name);
        dest.writeString(review_text);
        dest.writeString(user_rating);
        dest.writeString(review_time_friendly);
        dest.writeString(profile_image_url);
    }
}
