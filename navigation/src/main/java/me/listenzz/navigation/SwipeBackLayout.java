package me.listenzz.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Listen on 2018/7/1.
 * <p>
 * modified from https://github.com/ikew0ng/SwipeBackLayout
 *
 */
public class SwipeBackLayout extends FrameLayout {

    private static String TAG = "SwipeBackLayout";

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private static final int FULL_ALPHA = 255;

    /**
     * A view is not currently being dragged or animating as a result of a
     * fling/snap.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * A view is currently being dragged. The position is currently changing as
     * a result of user input or simulated user input.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * A view is currently settling into place as a result of a fling or
     * predefined non-interactive motion.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    /**
     * Default threshold of scroll
     */
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.45f;

    private static final float DEFAULT_PARALLAX = 0.5f;

    /**
     * Threshold of scroll, we will close the activity, when scrollPercent over
     * this value;
     */
    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    private float mParallaxOffset = DEFAULT_PARALLAX;

    private View mCapturedView;

    private ViewDragHelper mDragHelper;

    private float mScrollPercent;

    private int mLeft;

    /**
     * The set of listeners to be sent events through.
     */
    private SwipeListener mListener;

    private Drawable mShadowLeft;

    private Drawable mTabBar;

    private Rect mTabBarOriginBounds;

    private float mScrimOpacity;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private boolean mInLayout;

    private Rect mTmpRect = new Rect();

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mShadowLeft = ContextCompat.getDrawable(context, R.drawable.nav_shadow_left);

        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
        final float density = getResources().getDisplayMetrics().density;
        final float minVelocity = MIN_FLING_VELOCITY * density;
        mDragHelper.setMinVelocity(minVelocity);
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    public void setTabBar(Drawable drawable) {
        this.mTabBar = drawable;
        if (drawable != null) {
            mTabBarOriginBounds = drawable.copyBounds();
        }
    }

    public void setParallaxOffset(float offset) {
        mParallaxOffset = offset;
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    public void setSwipeListener(SwipeListener listener) {
        mListener = listener;
    }

    public interface SwipeListener {

        void onViewDragStateChanged(int state, float scrollPercent);

        boolean shouldSwipeBack();
    }

    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mListener.shouldSwipeBack()) {
            return super.onInterceptTouchEvent(event);
        }

        try {
            return mDragHelper.shouldInterceptTouchEvent(event);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mListener.shouldSwipeBack()) {
            return super.onTouchEvent(event);
        }
        mDragHelper.processTouchEvent(event);
        return mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mInLayout = true;
        if (mCapturedView != null)
            mCapturedView.layout(mLeft, 0,
                    mLeft + mCapturedView.getMeasuredWidth(),
                    mCapturedView.getMeasuredHeight());
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mCapturedView;
        int index = indexOfChild(child);
        if (mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE && index == getChildCount() -2) {
            View lastChild = getChildAt(getChildCount() -1);
            canvas.save();
            canvas.clipRect(0, 0, lastChild.getLeft(), getHeight());
        }

        boolean ret = super.drawChild(canvas, child, drawingTime);

        if (mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE && index == getChildCount() -2) {
            canvas.restore();
        }

        if (mTabBar != null && drawContent) {
            drawTabBar(canvas, child);
        }

        if (mScrimOpacity > 0 && drawContent
                && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return ret;
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);
        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);
        mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                childRect.left, childRect.bottom);
        mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
        mShadowLeft.draw(canvas);
    }

    private void drawTabBar(Canvas canvas, View child) {
        canvas.save();
        canvas.clipRect(0, 0, child.getLeft(), getHeight());
        int leftOffset = (int) ((mCapturedView.getLeft() - getWidth()) * mParallaxOffset * mScrimOpacity);
        mTabBar.setBounds(leftOffset, mTabBarOriginBounds.top, mTabBarOriginBounds.right + leftOffset, mTabBarOriginBounds.bottom );
        mTabBar.draw(canvas);
        canvas.restore();
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = 1 - mScrollPercent;
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        int count = getChildCount();
        if (mScrimOpacity >= 0 && mCapturedView != null && count > 1) {
            int leftOffset = (int) ((mCapturedView.getLeft() - getWidth()) * mParallaxOffset * mScrimOpacity);
            View underlying = getChildAt(count -2);
            underlying.setX(leftOffset > 0 ? 0 : leftOffset);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View view, int pointerId) {
            boolean ret = mDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId);
            boolean directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, pointerId);
            return  mDragHelper.getViewDragState() != ViewDragHelper.STATE_SETTLING &&  (ret & directionCheck);
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            mCapturedView = capturedChild;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return child.getWidth();
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            mScrollPercent = Math.abs((float) left / mCapturedView.getWidth());
            mLeft = left;
            invalidate();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();
            int left = xvel > 0 || (xvel == 0 && mScrollPercent > mScrollThreshold) ? childWidth : 0;
            mDragHelper.settleCapturedViewAt(left, 0);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return Math.min(child.getWidth(), Math.max(left, 0));
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                mCapturedView = null;
                mLeft = 0;
                int count = getChildCount();
                if (count > 1) {
                    View underlying = getChildAt(count - 2);
                    underlying.setX(0);
                }
            }
            mListener.onViewDragStateChanged(state, mScrollPercent);
        }
    }
}
