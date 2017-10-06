package com.example.dangkhoa.placestogo.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class PlaceDetail implements Parcelable {

    private String id;
    private String name;
    private String address;
    private String image_url;

    private double latitude;
    private double longitude;

    private double rating;

    private int opening; // 0 if closed

    private String website;

    private String locality;
    private String country;
    private String postCode;

    private String internationalPhone;

    private ArrayList<Review> reviews = new ArrayList<>();


    public PlaceDetail() {

    }

    public PlaceDetail(String id, String name, String address, String image_url, double latitude, double longitude, double rating, int opening) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.image_url = image_url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.opening = opening;

        this.website = null;
        this.locality = null;
        this.postCode = null;
        this.internationalPhone = null;
        this.reviews = new ArrayList<>();
    }

    protected PlaceDetail(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        image_url = in.readString();
        rating = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        opening = in.readInt();
        website = in.readString();
        locality = in.readString();
        country = in.readString();
        postCode = in.readString();
        internationalPhone = in.readString();
        in.readTypedList(reviews, Review.CREATOR);
    }

    public static final Parcelable.Creator<PlaceDetail> CREATOR = new Parcelable.Creator<PlaceDetail>() {
        @Override
        public PlaceDetail createFromParcel(Parcel in) {
            return new PlaceDetail(in);
        }

        @Override
        public PlaceDetail[] newArray(int size) {
            return new PlaceDetail[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getOpening() {
        return opening;
    }

    public void setOpening(int opening) {
        this.opening = opening;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getInternationalPhone() {
        return internationalPhone;
    }

    public void setInternationalPhone(String internationalPhone) {
        this.internationalPhone = internationalPhone;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(image_url);
        dest.writeDouble(rating);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(opening);
        dest.writeString(website);
        dest.writeString(locality);
        dest.writeString(country);
        dest.writeString(postCode);
        dest.writeString(internationalPhone);
        dest.writeTypedList(reviews);
    }

}
