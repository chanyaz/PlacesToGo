package com.example.dangkhoa.placestogo.RecyclerViewDecoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dangkhoa on 01/10/2017.
 */

public class PlaceListItemSpacing extends RecyclerView.ItemDecoration {

    private final int mSpacing;

    public PlaceListItemSpacing(int spacing) {
        this.mSpacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = mSpacing;
        outRect.right = mSpacing;
        outRect.bottom = mSpacing;

        if (parent.getChildAdapterPosition(view)%2 == 0) {
            outRect.left = mSpacing*2;
        } else {
            outRect.right = mSpacing*2;
        }

        if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
            // double spacing for the first 2 items
            outRect.top = mSpacing*2;
        }  else {
            // double spacing between top and bottom of other items
            outRect.top = mSpacing;
        }
    }
}
