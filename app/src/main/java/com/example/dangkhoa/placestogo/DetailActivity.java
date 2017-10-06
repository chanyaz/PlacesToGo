package com.example.dangkhoa.placestogo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.dangkhoa.placestogo.data.PlaceDetail;

public class DetailActivity extends AppCompatActivity {

    private static final String DETAIL_FRAGMENT_TAG = "detail_fragment_tag";

    public static final String INTENT_PACKAGE = "intent_package";
    public static final String PLACE_BUNDLE_KEY = "place_bundle_key";
    public static final String FLAG_KEY = "flag_key";

    private PlaceDetail mPlaceDetail;
    private int mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getBundleExtra(INTENT_PACKAGE);
            mPlaceDetail = bundle.getParcelable(PLACE_BUNDLE_KEY);
            mFlag = bundle.getInt(FLAG_KEY);

            DetailFragment detailFragment = new DetailFragment();

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.PLACE_ARG_BUNDLE_KEY, mPlaceDetail);
            arguments.putInt(DetailFragment.FLAG_ARG_BUNDLE_KEY, mFlag);

            detailFragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .replace(R.id.detail_activity_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {

        }

    }
}
