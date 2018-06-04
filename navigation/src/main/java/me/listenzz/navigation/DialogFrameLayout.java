package me.listenzz.navigation;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DialogFrameLayout extends FrameLayout {

    public static final String TAG = "Navigation";

    interface OnTouchOutsideListener {
        void onTouchOutside();
    }

    GestureDetector gestureDetector = null;

    OnTouchOutsideListener onTouchOutsideListener;

    boolean assumeNoHit = false;

    public void setAssumeNoHit(boolean assumeNoHit) {
        this.assumeNoHit = assumeNoHit;
    }

    public void setOnTouchOutsideListener(OnTouchOutsideListener onTouchOutsideListener) {
        this.onTouchOutsideListener = onTouchOutsideListener;
    }

    public DialogFrameLayout(@NonNull Context context) {
        super(context);
        commonInit(context);
    }

    private void commonInit(@NonNull Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.i(TAG, "onDown");
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Rect rect = new Rect();
                getHitRect(rect);
                Log.i(TAG, "onSingleTapUp:" + rect);
                if (!assumeNoHit) {
                    int count = getChildCount();
                    for (int i = count - 1; i > -1; i--) {
                        View child = getChildAt(i);
                        Rect outRect = new Rect();
                        child.getHitRect(outRect);
                        Log.i(TAG, "child rect:" + outRect);
                        if (outRect.contains((int) e.getX(), (int) e.getY())) {
                            Log.i(TAG, "hit child!!!!");
                            return false;
                        }
                    }
                }
                if (onTouchOutsideListener != null) {
                    onTouchOutsideListener.onTouchOutside();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
