package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.R;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 13/02/2018.
 */

public class OpeningHoursAdapter extends RecyclerView.Adapter<OpeningHoursAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> openingHoursList;

    public OpeningHoursAdapter(Context context, ArrayList<String> list) {
        mContext = context;
        openingHoursList = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dayText;

        public ViewHolder(View itemView) {
            super(itemView);

            dayText = itemView.findViewById(R.id.openingHoursTextViewItem);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.opening_hours_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.dayText.setText(openingHoursList.get(position));
    }

    @Override
    public int getItemCount() {
        return openingHoursList.size();
    }
}
