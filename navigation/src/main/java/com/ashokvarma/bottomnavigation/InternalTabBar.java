package com.ashokvarma.bottomnavigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.listenzz.navigation.AppUtils;
import me.listenzz.navigation.AwesomeActivity;
import me.listenzz.navigation.R;

/**
 * Created by Listen on 2018/8/27.
 */

public class InternalTabBar extends BottomNavigationBar {

    private Drawable shadow = new ColorDrawable(Color.parseColor("#dddddd"));
    private List<TextView> redPoints = new ArrayList<>();

    public InternalTabBar(Context context) {
        super(context);
    }

    public InternalTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InternalTabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InternalTabBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(0);
        }
        if (shadow != null) {
            int height = (int) getContext().getResources().getDisplayMetrics().density;
            shadow.setBounds(0, 0, getWidth(), height);
            shadow.draw(canvas);
        }
    }

    public void setShadow(@Nullable Drawable drawable) {
        shadow = drawable;
        postInvalidate();
    }


    private boolean isInitialized;

    @Override
    public void initialise() {
        super.initialise();
        isInitialized = true;

        int count = mBottomNavigationTabs.size();
        Context context = getContext();

        for (int i = 0; i < count; i++) {
            BottomNavigationTab tab = mBottomNavigationTabs.get(i);
            tab.iconView.setScaleType(ImageView.ScaleType.CENTER);
            TextView redPoint = new TextView(context);
            int size = AppUtils.dp2px(context, 10);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
            layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
            layoutParams.topMargin = AppUtils.dp2px(context, 4);
            layoutParams.rightMargin = AppUtils.dp2px(context, 2);
            tab.iconContainerView.addView(redPoint, layoutParams);
            Drawable drawable = ContextCompat.getDrawable(context, me.listenzz.navigation.R.drawable.nav_red_point);
            if (context instanceof AwesomeActivity && drawable != null) {
                AwesomeActivity activity = (AwesomeActivity) context;
                drawable.setColorFilter(Color.parseColor(activity.getStyle().getBadgeColor()), PorterDuff.Mode.SRC_IN);
            }
            redPoint.setBackground(drawable);
            redPoint.setVisibility(GONE);
            redPoints.add(redPoint);
        }
    }

    public ImageView imageViewAtTab(int index) {
        return mBottomNavigationTabs.get(index).iconView;
    }

    // only call this method after #initialise
    public void setTabIcon(int index, Drawable drawable, Drawable inactiveDrawable) {
        ImageView imageView = imageViewAtTab(index);
        BottomNavigationTab tab = mBottomNavigationTabs.get(index);
        if (inactiveDrawable != null) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected},
                    drawable);
            states.addState(new int[]{-android.R.attr.state_selected},
                    inactiveDrawable);
            states.addState(new int[]{},
                    inactiveDrawable);
            imageView.setImageDrawable(states);
        } else {
            DrawableCompat.setTintList(drawable, new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //1
                            new int[]{-android.R.attr.state_selected}, //2
                            new int[]{}
                    },
                    new int[]{
                            getActiveColor(), //1
                            getInActiveColor(), //2
                            getInActiveColor() //3
                    }
            ));
            imageView.setImageDrawable(drawable);
        }
    }

    public void setTabItemColor(String activeColor, String inActiveColor) {

        setActiveColor(activeColor);
        setInActiveColor(inActiveColor);
        int count = mBottomNavigationTabs.size();
        for (int i = 0; i < count; i++) {
            setTabItemColor(i, Color.parseColor(activeColor), Color.parseColor(inActiveColor));
        }
    }

    public void setTabItemColor(int index, @ColorInt int activeColor, @ColorInt int inActiveColor) {
        BottomNavigationTab tab = mBottomNavigationTabs.get(index);

        ImageView imageView = imageViewAtTab(index);
        Drawable drawable = imageView.getDrawable();
        if (!(drawable instanceof StateListDrawable)) {
            DrawableCompat.setTintList(drawable, new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //1
                            new int[]{-android.R.attr.state_selected}, //2
                            new int[]{}
                    },
                    new int[]{
                            activeColor, //1
                            inActiveColor, //2
                            inActiveColor //3
                    }
            ));
            imageView.setImageDrawable(drawable);
        }
        tab.setActiveColor(activeColor);
        tab.setInactiveColor(inActiveColor);
        tab.labelView.setTextColor(tab.isActive ? activeColor : inActiveColor);
    }

    public void setTabItemColor(@ColorRes int activeColor, @ColorRes int inActiveColor) {
        setActiveColor(activeColor);
        setInActiveColor(inActiveColor);
        int count = mBottomNavigationTabs.size();
        for (int i = 0; i < count; i++) {
            setTabItemColor(i, ContextCompat.getColor(getContext(), activeColor), ContextCompat.getColor(getContext(), inActiveColor));
        }
    }

    public TextView badgeViewAtTab(int index) {
        BottomNavigationTab tab = mBottomNavigationTabs.get(index);
        return tab.badgeView;
    }

    public void setBadge(int index, String text) {
        TextView badgeView = badgeViewAtTab(index);
        if (TextUtils.isEmpty(text)) {
            badgeView.setVisibility(GONE);
        } else {
            badgeView.setText(text);
            badgeView.setVisibility(VISIBLE);
        }
    }

    public TextView redPointAtTab(int index) {
        return redPoints.get(index);
    }

    public void setRedPoint(int index, boolean visible) {
        TextView redPoint = redPointAtTab(index);
        if (!visible) {
            redPoint.setVisibility(GONE);
        } else {
            redPoint.setVisibility(VISIBLE);
        }
    }

    public void setTabBarBackgroundColor(String color) {
        if (isInitialized) {
            View backgroundView = findViewById(R.id.bottom_navigation_bar_container);
            backgroundView.setBackgroundColor(Color.parseColor(color));
        } else {
            setBarBackgroundColor(color);
        }
    }
}
