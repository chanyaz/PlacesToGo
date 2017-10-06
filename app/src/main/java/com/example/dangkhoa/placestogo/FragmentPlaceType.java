package com.example.dangkhoa.placestogo;

import android.app.DialogFragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangkhoa.placestogo.adapter.PlaceTypeListAdapter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dangkhoa on 05/10/2017.
 */

public class FragmentPlaceType extends DialogFragment {

    public FragmentPlaceType() {

    }

    private PlaceTypeListAdapter mAdapter;
    private ViewHolder viewHolder;

    private Location mLocation;

    private ArrayList<String> mList;

    private class ViewHolder {

        public RecyclerView recyclerView;
        public Toolbar toolbar;

        public ViewHolder(View view) {
            recyclerView = view.findViewById(R.id.frgament_place_type_recyclerview);
            toolbar = view.findViewById(R.id.fragment_place_type_toolbar);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        mLocation = bundle.getParcelable(MainActivity.CURRENT_LOCATION_KEY);

        mList = new ArrayList<>();
        mAdapter = new PlaceTypeListAdapter(getContext(), mList, mLocation);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_type_list, container, false);

        viewHolder = new ViewHolder(view);

        ((AppCompatActivity) getContext()).setSupportActionBar(viewHolder.toolbar);
        ((AppCompatActivity) getContext()).getSupportActionBar().setTitle(getString(R.string.place_types));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewHolder.recyclerView.setLayoutManager(linearLayoutManager);

        Collections.addAll(mList, getContext().getResources().getStringArray(R.array.place_type_array));
        mAdapter.notifyItemRangeInserted(0, mList.size());

        viewHolder.recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    }
}
