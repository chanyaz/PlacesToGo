package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.dangkhoa.placestogo.DetailActivity;
import com.example.dangkhoa.placestogo.R;
import com.example.dangkhoa.placestogo.data.PlaceDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by dangkhoa on 28/09/2017.
 */

public class GooglePlacesAutoCompleteAdapter extends ArrayAdapter implements Filterable {

    public static final int FLAG_SEARCH_LIST_ADAPTER = 100;

    private ArrayList<Pair<String, String>> mList;

    private Context context;

    public GooglePlacesAutoCompleteAdapter(Context context) {
        super(context, 0);
        this.context = context;
        this.mList = null;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        if (mList == null) {
            return null;
        }
        return mList.get(position).second;
    }

    // https://examples.javacodegeeks.com/android/android-google-places-autocomplete-api-example/
    // https://developers.google.com/places/web-service/autocomplete

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if (charSequence != null) {
                    PlacesAsynctask asynctask = new PlacesAsynctask();
                    try {
                        mList = asynctask.execute(charSequence.toString()).get();

                        if (mList != null) {
                            filterResults.values = mList;
                            filterResults.count = mList.size();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return filterResults;
                }
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.list_item_search, parent, false);
        }

        if (mList != null) {
            TextView searchText = convertView.findViewById(R.id.searchItem);
            searchText.setText(mList.get(position).second);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    PlaceDetail placeDetail = new PlaceDetail();
                    placeDetail.setId(mList.get(position).first);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DetailActivity.PLACE_BUNDLE_KEY, placeDetail);
                    bundle.putInt(DetailActivity.FLAG_KEY, FLAG_SEARCH_LIST_ADAPTER);

                    intent.putExtra(DetailActivity.INTENT_PACKAGE, bundle);

                    context.startActivity(intent);
                }
            });
        }
        return convertView;
    }

    private class PlacesAsynctask extends AsyncTask<String, Void, ArrayList<Pair<String, String>>> {

        @Override
        protected ArrayList<Pair<String, String>> doInBackground(String... strings) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String json = null;

            try {
                String BASE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
                String KEY = "key";
                String INPUT = "input";

                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(KEY, context.getString(R.string.android_api_key))
                        .appendQueryParameter(INPUT, strings[0])
                        .build();

                URL url = new URL(uri.toString());

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();


                InputStream inputStream = httpURLConnection.getInputStream();
                if (inputStream == null)
                    return null;

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                json = stringBuilder.toString();
                Log.d("MAIN", json);

            } catch (Exception e) {

            } finally {
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (json != null) {
                return extractJson(json);
            } else {
                return null;
            }
        }

        private ArrayList<Pair<String, String>> extractJson(String json) {

            String PREDICTIONS = "predictions";
            String DESCRIPTION = "description";
            String PlACE_ID = "place_id";

            ArrayList<Pair<String, String>> resultList = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(json);
                JSONArray predsJsonArray = jsonObj.getJSONArray(PREDICTIONS);

                for (int i = 0; i < predsJsonArray.length(); i++) {

                    String place_id = predsJsonArray.getJSONObject(i).getString(PlACE_ID);
                    String desc = predsJsonArray.getJSONObject(i).getString(DESCRIPTION);

                    Pair<String, String> pair = new Pair(place_id, desc);

                    resultList.add(pair);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultList;

        }
    }
}
