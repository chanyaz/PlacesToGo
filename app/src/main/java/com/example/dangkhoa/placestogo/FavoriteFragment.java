package com.example.dangkhoa.placestogo;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dangkhoa.placestogo.RecyclerViewDecoration.FavoritePlaceItemSpacing;
import com.example.dangkhoa.placestogo.adapter.FavoritePlacesAdapter;
import com.example.dangkhoa.placestogo.database.DBContract;

/**
 * Created by dangkhoa on 23/10/2017.
 */

public class FavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_ID = 1;

    private FavoritePlacesAdapter adapter;
    private ViewHolder viewHolder;

    private LinearLayoutManager linearLayoutManager;

    private Paint paint;

    private class ViewHolder {
        public RecyclerView recyclerView;

        public ViewHolder(View view) {
            recyclerView = view.findViewById(R.id.favorite_recycler_view);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paint = new Paint();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        viewHolder = new ViewHolder(view);

        adapter = new FavoritePlacesAdapter(getContext(), null);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewHolder.recyclerView.setLayoutManager(linearLayoutManager);

        FavoritePlaceItemSpacing itemSpacing = new FavoritePlaceItemSpacing(5);
        viewHolder.recyclerView.addItemDecoration(itemSpacing);

        viewHolder.recyclerView.setAdapter(adapter);

        /*
            Swipe item to delete
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String id = (String) viewHolder.itemView.getTag();
                getContext().getContentResolver().delete(DBContract.PlacesEntry.buildItemUri(id), null, null);

                // restart loader when item is deleted
                getLoaderManager().restartLoader(LOAD_ID, null, FavoriteFragment.this);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    paint.setColor(getContext().getColor(R.color.red));
                    RectF background;
                    RectF icon_dest;

                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);

                    if (dX > 0) {
                        background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                    } else {
                        background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                    }

                    c.drawRect(background, paint);
                    c.drawBitmap(icon, null, icon_dest, paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        }).attachToRecyclerView(viewHolder.recyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOAD_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getContext(), DBContract.PlacesEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}