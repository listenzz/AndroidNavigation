package com.navigation.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import com.navigation.R;

/**
 * Created by Listen on 2018/1/9.
 */

public class Style implements Cloneable {

    private static int INVALID_COLOR = Integer.MAX_VALUE;
    private static Drawable defaultShadow = new ColorDrawable(Color.parseColor("#DDDDDD"));

    private int screenBackgroundColor = Color.WHITE;

    private BarStyle toolBarStyle = BarStyle.LightContent;
    private Drawable backIcon;
    private int statusBarColor = Color.TRANSPARENT;
    private int toolBarBackgroundColor = INVALID_COLOR;
    private int toolBarTintColor = INVALID_COLOR;
    private int titleTextColor = INVALID_COLOR;
    private int titleTextSize = 17;
    private float elevation = -1;
    private Drawable shadow = defaultShadow;

    private int titleGravity = Gravity.START;

    private int toolBarButtonItemTintColor = INVALID_COLOR;
    private int toolBarButtonItemTextSize = 15;

    private String bottomBarBackgroundColor = "#FFFFFF";
    private String bottomBarActiveColor = null;
    private String bottomBarInActiveColor = null;
    private Drawable bottomBarShadow = defaultShadow;

    // ------ screen ------

    public void setScreenBackgroundColor(int color) {
        screenBackgroundColor = color;
    }

    public int getScreenBackgroundColor() {
        return screenBackgroundColor;
    }

    // ----- tabBar  -----

    public void setBottomBarBackgroundColor(String bottomBarBackgroundColor) {
        this.bottomBarBackgroundColor = bottomBarBackgroundColor;
    }

    public String getBottomBarBackgroundColor() {
        return bottomBarBackgroundColor;
    }

    public void setBottomBarActiveColor(String bottomBarActiveColor) {
        this.bottomBarActiveColor = bottomBarActiveColor;
    }

    public String getBottomBarActiveColor() {
        return bottomBarActiveColor;
    }

    public String getBottomBarInActiveColor() {
        return bottomBarInActiveColor;
    }

    public void setBottomBarInActiveColor(String bottomBarInActiveColor) {
        this.bottomBarInActiveColor = bottomBarInActiveColor;
    }

    // ------- toolBar ---------

    public void setToolBarStyle(BarStyle barStyle) {
        toolBarStyle = barStyle;
    }

    public BarStyle getToolBarStyle() {
        return toolBarStyle;
    }

    public void setStatusBarColor(int color) {
        statusBarColor = color;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public void setToolBarBackgroundColor(int color) {
        toolBarBackgroundColor = color;
    }

    public int getToolBarBackgroundColor() {
        if (toolBarBackgroundColor != INVALID_COLOR) {
            return toolBarBackgroundColor;
        }

        if (toolBarStyle == BarStyle.LightContent) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    public void setElevation(Context context, float elevation) {
        this.elevation = elevation * context.getResources().getDisplayMetrics().density;
    }

    public float getElevation(Context context) {
        if (elevation != -1) {
            return elevation;
        }
        elevation = 2 * context.getResources().getDisplayMetrics().density;
        return elevation;
    }

    public void setToolBarTintColor(int color) {
        toolBarTintColor = color;
    }

    public int getToolBarTintColor() {
        if (toolBarTintColor != INVALID_COLOR) {
            return toolBarTintColor;
        }

        if (toolBarStyle == BarStyle.LightContent) {
            return Color.WHITE;
        } else {

            if (toolBarBackgroundColor == INVALID_COLOR) {
                return Color.parseColor("#666666");
            }

            return Color.BLACK;
        }
    }

    public void setBackIcon(Drawable icon) {
        backIcon = icon;
    }

    public Drawable getBackIcon(Context context) {
        if (backIcon != null) {
            return backIcon;
        }
        Drawable drawable = context.getResources().getDrawable(R.drawable.nav_ic_arrow_back);
        drawable.setColorFilter(getToolBarButtonItemTintColor(), PorterDuff.Mode.SRC_ATOP);
        backIcon = drawable;
        return backIcon;
    }

    public void setTitleTextColor(int color) {
        titleTextColor = color;
    }

    public int getTitleTextColor() {
        if (titleTextColor != INVALID_COLOR) {
            return titleTextColor;
        }

        return getToolBarTintColor();
    }

    public void setTitleTextSize(int dp) {
        titleTextSize = dp;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public Drawable getShadow() {
        return shadow;
    }

    public void setShadow(Drawable drawable) {
        this.shadow = drawable;
    }

    public Drawable getBottomBarShadow() {
        return bottomBarShadow;
    }

    public void setBottomBarShadow(Drawable drawable) {
        this.bottomBarShadow = drawable;
    }

    public void setToolBarButtonItemTintColor(int color) {
        toolBarButtonItemTintColor = color;
    }

    public int getToolBarButtonItemTintColor() {
        if (toolBarButtonItemTintColor != INVALID_COLOR) {
            return toolBarButtonItemTintColor;
        }
        return getToolBarTintColor();
    }

    public void setToolBarButtonItemTextSize(int dp) {
        toolBarButtonItemTextSize = dp;
    }

    public int getToolBarButtonItemTextSize() {
        return toolBarButtonItemTextSize;
    }

    public void setTitleGravity(int gravity) {
        titleGravity = gravity;
    }

    public int getTitleGravity() {
        return titleGravity;
    }

    @Override
    public Style clone() throws CloneNotSupportedException {
        return (Style) super.clone();
    }
}
