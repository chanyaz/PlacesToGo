package com.example.dangkhoa.placestogo.RecyclerViewDecoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dangkhoa on 24/10/2017.
 */

public class FavoritePlaceItemSpacing extends RecyclerView.ItemDecoration {

    private final int mSpacing;

    public FavoritePlaceItemSpacing(int spacing) {
        this.mSpacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = mSpacing*2;
        outRect.right = mSpacing*2;
        outRect.bottom = mSpacing;

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mSpacing*4;
        }
    }
}
