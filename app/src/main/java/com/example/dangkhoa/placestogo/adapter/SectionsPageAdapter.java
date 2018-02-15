package com.example.dangkhoa.placestogo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.dangkhoa.placestogo.CameraFragment;
import com.example.dangkhoa.placestogo.GalleryFragment;

/**
 * Created by dangkhoa on 10/02/2018.
 */

public class SectionsPageAdapter extends FragmentPagerAdapter {

    private int noOfTabs;

    public SectionsPageAdapter(FragmentManager fm, int noOfTabs) {
        super(fm);
        this.noOfTabs = noOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new GalleryFragment();
            case 1:
                return new CameraFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}
