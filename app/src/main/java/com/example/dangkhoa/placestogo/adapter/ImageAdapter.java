package com.example.dangkhoa.placestogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.example.dangkhoa.placestogo.R;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 10/02/2018.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> imagePathList;

    public ImageAdapter(Context context, ArrayList<String> imagePathList) {
        this.mContext = context;
        this.imagePathList = imagePathList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView galleryImage;

        public ViewHolder(View itemView) {
            super(itemView);

            galleryImage = itemView.findViewById(R.id.imageViewGallery);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.image_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GlideApp.with(mContext)
                .load(imagePathList.get(position))
                .thumbnail(0.0001f)
                .into(holder.galleryImage);
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }
}
