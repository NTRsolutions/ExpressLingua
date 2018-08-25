package com.neosolusi.expresslingua.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemTouchDownListener implements RecyclerView.OnItemTouchListener {

    private OnItemTouchDownListener clickListener;
    private RecyclerView mView;
    private GestureDetector mGestureDetector;

    public RecyclerItemTouchDownListener(Context context, OnItemTouchDownListener listener) {
        clickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) {
                Log.d("RecyclerItemTouchDown", "Touch down confirmed");
                View childView = mView.findChildViewUnder(e.getX(), e.getY());
                clickListener.onItemTouchDown(childView, mView.getChildAdapterPosition(childView));
                return true;
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        mView = view;
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && clickListener != null && mGestureDetector.onTouchEvent(e)) {
            clickListener.onItemTouchDown(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemTouchDownListener {
        void onItemTouchDown(View view, int position);
    }
}
