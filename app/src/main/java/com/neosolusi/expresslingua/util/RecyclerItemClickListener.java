package com.neosolusi.expresslingua.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener clickListener;
    private RecyclerView mView;
    private GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        clickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                View childView = mView.findChildViewUnder(e.getX(), e.getY());
                clickListener.onItemClick(childView, mView.getChildAdapterPosition(childView));
                return true;
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        mView = view;
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && clickListener != null && mGestureDetector.onTouchEvent(e)) {
            clickListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
