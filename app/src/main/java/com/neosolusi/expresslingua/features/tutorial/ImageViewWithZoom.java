package com.neosolusi.expresslingua.features.tutorial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ImageViewWithZoom extends View {

    private Drawable image;
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;

    public ImageViewWithZoom(Context context) {
        super(context);
    }

    public ImageViewWithZoom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setImageDrawable(Drawable drawable) {
        image = drawable;
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Set the image bounderies
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        image.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        invalidate();

        return true;
    }

    @Override public boolean performClick() {
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // don't let the object get too small or too large.
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            scaleFactor = scaleFactor < 1 ? 1 : scaleFactor;

            invalidate();
            return true;
        }
    }

}
