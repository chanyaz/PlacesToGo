package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangkhoa.placestogo.Utils.Util;
import com.example.dangkhoa.placestogo.data.PlaceDetail;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 21/01/2018.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final String PLACE_LIST_KEY = "place_list_key";
    public static final String LOCATION_KEY = "location_key";
    public static final String RADIUS_KEY = "radius_key";

    private GoogleMap mGoogleMap;
    private MapView mapView;
    private LatLng mLocation;

    private ArrayList<PlaceDetail> list;
    private float radius;

    private static final float ZOOM = 14;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Bundle args = getArguments();

        list = args.getParcelableArrayList(PLACE_LIST_KEY);
        mLocation = Util.stringToLocation(args.getString(LOCATION_KEY));
        radius = args.getFloat(RADIUS_KEY);

        mapView = view.findViewById(R.id.places_map);
        mapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        CameraPosition position = new CameraPosition.Builder().target(mLocation).zoom(ZOOM).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        addCurrentLocationMarker();
        addRestaurantMarkers();

        mGoogleMap.addCircle(new CircleOptions()
                .center(mLocation)
                .radius((double) radius)
                .strokeColor(getContext().getColor(R.color.deep_purple_700))
                .fillColor(Color.TRANSPARENT)
        );
    }

    private void addCurrentLocationMarker() {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(mLocation)
                .title(getString(R.string.current_location))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_blue));
        mGoogleMap.addMarker(markerOptions);
    }

    private void addRestaurantMarkers() {
        for (int i = 0; i < list.size(); i++) {
            LatLng latLng = new LatLng(list.get(i).getLatitude(), list.get(i).getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(list.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_orange));
            mGoogleMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
