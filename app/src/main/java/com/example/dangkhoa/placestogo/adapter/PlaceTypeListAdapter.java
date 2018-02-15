package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.MainFragment;
import com.example.dangkhoa.placestogo.PlaceListActivity;
import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Utils.Util;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 05/10/2017.
 */

public class PlaceTypeListAdapter extends RecyclerView.Adapter<PlaceTypeListAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private ArrayList<String> mList;
    private ArrayList<String> mFilteredList;

    private Location mLastLocation;

    public  PlaceTypeListAdapter(Context context, ArrayList<String> list, Location location) {
        mContext = context;
        mList = list;
        mFilteredList = list;
        mLastLocation = location;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.place_type_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String placeType = mFilteredList.get(position);

        holder.mTextView.setText(Util.placeTypeFromValueToLabel(placeType));
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    mFilteredList = mList;
                } else {
                    ArrayList<String> filteredList = new ArrayList<>();

                    for (String placeValue : mList) {
                        if (Util.placeTypeFromValueToLabel(placeValue).toLowerCase().contains(charString)) {
                            filteredList.add(placeValue);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = itemView.findViewById(R.id.place_type_textView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            Intent intent = new Intent(mContext, PlaceListActivity.class);
            intent.putExtra(MainFragment.PLACE_TYPE_KEY, mFilteredList.get(position));
            intent.putExtra(MainFragment.CURRENT_LOCATION_KEY, mLastLocation);
            mContext.startActivity(intent);
        }
    }
}
