package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.Utils.Util;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by dangkhoa on 13/02/2018.
 */

public class OpeningHoursAdapter extends RecyclerView.Adapter<OpeningHoursAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> openingHoursList;

    private int dayOfWeek;
    private int indexToBeHighLighted;
    private int[] daysOfWeek = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    public OpeningHoursAdapter(Context context, ArrayList<String> list) {
        mContext = context;
        openingHoursList = list;

        dayOfWeek = Util.getDayOfWeek();
        indexToBeHighLighted = getIndexToBeHighLighted();
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

        View view = inflater.inflate(R.layout.list_item_opening_hours, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == indexToBeHighLighted) {
            holder.dayText.setTextColor(mContext.getColor(R.color.colorPrimary));
        }
        holder.dayText.setText(openingHoursList.get(position));
    }

    @Override
    public int getItemCount() {
        return openingHoursList.size();
    }

    private int getIndexToBeHighLighted() {
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (dayOfWeek == daysOfWeek[i]) {
                return i;
            }
        }
        return -1;
    }
}
