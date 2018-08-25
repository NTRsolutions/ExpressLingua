package com.neosolusi.expresslingua.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemDoubleClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemDoubleClickListener doubleClickListener;
    private GestureDetector mGestureDetector;

    public RecyclerItemDoubleClickListener(Context context, OnItemDoubleClickListener listener) {
        doubleClickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && doubleClickListener != null && mGestureDetector.onTouchEvent(e)) {
            doubleClickListener.onItemDoubleClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemDoubleClickListener {
        void onItemDoubleClick(View view, int position);
    }
}
