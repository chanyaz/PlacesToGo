package com.example.dangkhoa.placestogo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.dangkhoa.placestogo.Utils.FileUtil;
import com.example.dangkhoa.placestogo.adapter.GlideApp;
import com.example.dangkhoa.placestogo.adapter.ImageAdapter;

import java.util.ArrayList;

/**
 * Created by dangkhoa on 10/02/2018.
 */

public class GalleryFragment extends Fragment {

    private ArrayList<String> directories;
    private ArrayList<String> imagePaths;

    private ImageAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;

    private ViewHolder viewHolder;

    private class ViewHolder {
        public RecyclerView mRecyclerView;
        public Spinner directorySpinner;
        public ImageView previewImageView;

        public ViewHolder(View view) {
            mRecyclerView = view.findViewById(R.id.galleryRecyclerView);
            directorySpinner = view.findViewById(R.id.directorySpinner);
            previewImageView = view.findViewById(R.id.previewImageView);
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        directories = new ArrayList<>();
        imagePaths = new ArrayList<>();

        mAdapter = new ImageAdapter(getContext(), imagePaths);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        viewHolder = new ViewHolder(view);

        mGridLayoutManager = new GridLayoutManager(getContext(), 4, StaggeredGridLayoutManager.VERTICAL, false);
        viewHolder.mRecyclerView.setLayoutManager(mGridLayoutManager);
        viewHolder.mRecyclerView.setHasFixedSize(true);

        initDirectorySpinner();

        return view;
    }

    private void initDirectorySpinner() {

        // directory for Gallery
        directories.add(FileUtil.DCIM);

        if (FileUtil.getDirectoryPaths(FileUtil.DCIM) != null) {
            directories.addAll(FileUtil.getDirectoryPaths(FileUtil.DCIM));
        }

        if (FileUtil.getDirectoryPaths(FileUtil.PICTURES) != null) {
            directories.addAll(FileUtil.getDirectoryPaths(FileUtil.PICTURES));
        }

        final ArrayList<String> directoryNames = new ArrayList<>();
        directoryNames.add(getString(R.string.gallery_title));

        // starting from index 1. Index 0 is the Gallery
        for (int i = 1; i < directories.size(); i++) {
            directoryNames.add(FileUtil.getDirectoryName(directories.get(i)));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.directorySpinner.setAdapter(adapter);

        viewHolder.directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                LoadImageAsyncTask task = new LoadImageAsyncTask();
                task.execute(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private class LoadImageAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> imagePathsAsync = new ArrayList<>();
            String directory = strings[0];

            // gallery is chosen and we need to get all images in the memory
            if (directory.equals(FileUtil.DCIM)) {
                ArrayList<String> directoriesAsync = new ArrayList<>();
                directoriesAsync.addAll(directories);
                // index 0 is the path of DCIM which is useless
                directoriesAsync.remove(0);

                imagePathsAsync.addAll(FileUtil.getFilePathsInManyDirectories(directoriesAsync));
            } else {
                imagePathsAsync.addAll(FileUtil.getFilePaths(directory));
            }
            return imagePathsAsync;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);

            int currentSize = imagePaths.size();
            imagePaths.clear();
            mAdapter.notifyItemRangeRemoved(0, currentSize);

            imagePaths.addAll(list);
            mAdapter.notifyItemRangeInserted(0, list.size());

            viewHolder.mRecyclerView.setAdapter(mAdapter);

            if (imagePaths.size() > 0) {
                GlideApp.with(getActivity())
                        .load(imagePaths.get(0))
                        .into(viewHolder.previewImageView);
            }
        }
    }
}
