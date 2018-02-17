package com.example.dangkhoa.placestogo.Utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.example.dangkhoa.placestogo.adapter.GlideApp;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.example.dangkhoa.placestogo.data.Review;
import com.example.dangkhoa.placestogo.data.User;
import com.example.dangkhoa.placestogo.database.DBContract;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 13/02/2018.
 */

public class FirebaseUtil {

    public static final String USERS_CHILD = "Users";
    public static final String USER_DETAIL_CHILD = "UserDetail";
    public static final String PROFILE_PHOTO_CHILD = "ProfilePhoto";
    public static final String FAVORITE_PLACES_CHILD = "FavoritePlaces";

    public static final String PLACES_CHILD = "Places";
    public static final String PLACE_DETAIL_CHILD = "PlaceDetail";
    public static final String REVIEWS_CHILD = "Reviews";
    public static final String GOOGLE_REVIEWS_CHILD = "GoogleReviews";
    public static final String NATIVE_REVIEWS_CHILD = "NativeReviews";

    private static final String GOOGLE_USER_REVIEW_ID = "Google_User_Review_";

    public static final String DEFAULT_USER_PROFILE_PHOTO_DOWNLOAD_LINK = "https://firebasestorage.googleapis.com/v0/b/placestogo-39f9d.appspot.com/o/DefaultPhoto%2Fuser_icon.png?alt=media&token=d879e74f-b6b8-4a23-87b5-816e2e5a6a4b";

    /**
     * Sign out clean up
     *
     * @param context
     */
    public static void signOut(Context context) {
        // delete all entries within table Places in sql database
        context.getContentResolver().delete(DBContract.PlacesEntry.CONTENT_URI, null, null);
        AuthUI.getInstance().signOut(context);
    }

    /**
     * Set user's profile picture
     *
     * @param context
     * @param mFirebaseAuth
     * @param imageView
     */
    public static void setProfilePicture(Context context, FirebaseAuth mFirebaseAuth, ImageView imageView) {
        if (mFirebaseAuth.getCurrentUser().getPhotoUrl() != null && !mFirebaseAuth.getCurrentUser().getPhotoUrl().equals("")) {
            GlideApp.with(context)
                    .load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                    .circleCrop()
                    .into(imageView);

            addUser(mFirebaseAuth);
        } else {
            setUserProfilePhotoFirebase(context, mFirebaseAuth, Uri.parse(DEFAULT_USER_PROFILE_PHOTO_DOWNLOAD_LINK), imageView);
        }
    }

    /**
     * Upload user's new profile picture
     *
     * @param context
     * @param mFirebaseAuth
     * @param uploadUri
     * @param imageView
     */
    public static void uploadProfilePicture(final Context context, final FirebaseAuth mFirebaseAuth, Uri uploadUri, final ImageView imageView) {
        StorageReference mUserProfilePhotoReference = FirebaseStorage.getInstance().getReference()
                .child(FirebaseUtil.USERS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(FirebaseUtil.PROFILE_PHOTO_CHILD);

        mUserProfilePhotoReference.putFile(uploadUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();

                        setUserProfilePhotoFirebase(context, mFirebaseAuth, downloadUri, imageView);
                    }
                });
    }

    public static void setUserProfilePhotoFirebase(final Context context, final FirebaseAuth firebaseAuth, Uri uri, final ImageView imageView) {

        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        firebaseAuth.getCurrentUser().updateProfile(userProfileChangeRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        GlideApp.with(context)
                                .load(firebaseAuth.getCurrentUser().getPhotoUrl())
                                .circleCrop()
                                .into(imageView);

                        addUser(firebaseAuth);
                    }
                });
    }

    /**
     * Add user's information including photo url, name and email
     *
     * @param mFirebaseAuth
     */
    public static void addUser(FirebaseAuth mFirebaseAuth) {
        User user = new User();
        user.setProfilePhoto(mFirebaseAuth.getCurrentUser().getPhotoUrl().toString());
        user.setUsername(mFirebaseAuth.getCurrentUser().getDisplayName());
        user.setEmail(mFirebaseAuth.getCurrentUser().getEmail());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(USERS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(USER_DETAIL_CHILD);

        userRef.setValue(user);
    }

    /**
     * Add a place to favorite list
     *
     * @param mFirebaseAuth
     * @param placeDetail
     */
    public static void addFavoritePlace(FirebaseAuth mFirebaseAuth, PlaceDetail placeDetail) {
        // get FavoritePlaces Node
        DatabaseReference favoritePlacesRef = FirebaseDatabase.getInstance().getReference()
                .child(USERS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(FAVORITE_PLACES_CHILD)
                .child(placeDetail.getId());
        // push id of place and set the value = name
        favoritePlacesRef.setValue(placeDetail.getName());

        addPlaceToPlacesNode(placeDetail);
    }

    /**
     * Add a place to Places Node if it does not exist
     *
     * @param placeDetail
     */
    public static void addPlaceToPlacesNode(PlaceDetail placeDetail) {
        // get PlaceID Node
        DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference()
                .child(PLACES_CHILD)
                .child(placeDetail.getId());

        // store google reviews
        /*for (int i = 0; i < placeDetail.getReviews().size(); i++) {
            String id = GOOGLE_USER_REVIEW_ID + i;

            placeRef.child(REVIEWS_CHILD)
                    .child(GOOGLE_REVIEWS_CHILD)
                    .child(id)
                    .setValue(placeDetail.getReviews().get(i));
        }*/

        ArrayList<Review> backupList = placeDetail.getReviews();

        placeDetail.setReviews(null);
        // store place detail
        placeRef.child(PLACE_DETAIL_CHILD).setValue(placeDetail);

        placeDetail.setReviews(backupList);
    }

    /**
     * Add user's review to a place
     * If the place reviewed is not in the Places Node, we'll add it as well
     *
     * @param mFirebaseAuth
     * @param placeDetail
     * @param review
     */
    public static void addUserReview(FirebaseAuth mFirebaseAuth, PlaceDetail placeDetail, Review review) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference()
                .child(PLACES_CHILD)
                .child(placeDetail.getId())
                .child(REVIEWS_CHILD)
                .child(NATIVE_REVIEWS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid());

        // don't need to store image here because we'lll query to get user's image at live time to get updated with the newest image the user is using
        review.setProfile_image_url(null);
        reviewsRef.setValue(review);

        // if the place that user reviews does not exist, add it
        addPlaceToPlacesNode(placeDetail);
    }

    /**
     * Remove a place from Favorite list
     *
     * @param mFirebaseAuth
     * @param placeID
     */
    public static void removePlaceFromFavorite(FirebaseAuth mFirebaseAuth, String placeID) {
        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference()
                .child(USERS_CHILD)
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(FAVORITE_PLACES_CHILD)
                .child(placeID);
        favRef.removeValue();
    }
}
