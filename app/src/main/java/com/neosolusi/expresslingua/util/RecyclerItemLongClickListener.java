package com.neosolusi.expresslingua.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemLongClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemLongClickListener longClickListener;
    private RecyclerView mView;
    private GestureDetector mGestureDetector;

    public RecyclerItemLongClickListener(Context context, OnItemLongClickListener listener) {
        longClickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public void onLongPress(MotionEvent e) {
                View childView = mView.findChildViewUnder(e.getX(), e.getY());
                longClickListener.onItemLongClick(childView, mView.getChildAdapterPosition(childView));
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        mView = view;
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && longClickListener != null && mGestureDetector.onTouchEvent(e)) {
            longClickListener.onItemLongClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
