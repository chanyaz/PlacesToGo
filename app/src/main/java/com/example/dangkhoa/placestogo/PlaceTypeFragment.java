package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangkhoa.placestogo.adapter.PlaceTypeListAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dangkhoa on 05/10/2017.
 */

public class PlaceTypeFragment extends Fragment {

    public PlaceTypeFragment() {

    }

    private PlaceTypeListAdapter mAdapter;
    private ViewHolder viewHolder;

    private Location mLocation;

    private ArrayList<String> mList;

    private class ViewHolder {

        public RecyclerView recyclerView;
        public SearchView searchView;

        public ViewHolder(View view) {
            recyclerView = view.findViewById(R.id.frgament_place_type_recyclerview);
            searchView = view.findViewById(R.id.search_place_type);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        mLocation = bundle.getParcelable(MainFragment.CURRENT_LOCATION_KEY);

        mList = new ArrayList<>();
        mAdapter = new PlaceTypeListAdapter(getContext(), mList, mLocation);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment_place_type, container, false);

        viewHolder = new ViewHolder(view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewHolder.recyclerView.setLayoutManager(linearLayoutManager);

        Collections.addAll(mList, getContext().getResources().getStringArray(R.array.place_type_array));
        mAdapter.notifyItemRangeInserted(0, mList.size());

        viewHolder.recyclerView.setAdapter(mAdapter);

        viewHolder.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);

                return true;
            }
        });

        return view;
    }
}
