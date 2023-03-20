package com.navigation.androidx;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class Style implements Cloneable {

    public static int INVALID_COLOR = Integer.MAX_VALUE;
    private static final Drawable defaultShadow = new ColorDrawable(Color.parseColor("#F0F0F0"));

    private int screenBackgroundColor = Color.WHITE;

    private int toolbarHeight;

    private BarStyle statusBarStyle;
    private int statusBarColor;
    private boolean statusBarColorAnimated = true;
    private boolean statusBarHidden = false;

    private int navigationBarColor = INVALID_COLOR;
    private boolean navigationBarHidden = false;

    private int toolbarBackgroundColor;
    private int toolbarBackgroundColorDarkContent = INVALID_COLOR;
    private int toolbarBackgroundColorLightContent = INVALID_COLOR;
    private Drawable backIcon;
    private int toolbarTintColor = INVALID_COLOR;
    private int toolbarTintColorDarkContent = INVALID_COLOR;
    private int toolbarTintColorLightContent = INVALID_COLOR;
    private int titleTextColor = INVALID_COLOR;
    private int titleTextColorDarkContent = INVALID_COLOR;
    private int titleTextColorLightContent = INVALID_COLOR;
    private int titleTextSize = 17;
    private int elevation = -1;
    private int titleGravity = Gravity.START;
    private int toolbarButtonTextSize = 15;
    private float toolbarAlpha = 1.f;
    private boolean toolbarShadowHidden = false;

    private String tabBarBackgroundColor = "#FFFFFF";
    private String tabBarItemColor = null;
    private String tabBarUnselectedItemColor = null;
    private Drawable tabBarShadow = defaultShadow;

    private final Context context;

    private boolean swipeBackEnabled;
    private String tabBarBadgeColor = "#FF3B30";

    private int scrimAlpha = 25;

    private boolean displayCutoutWhenLandscape = true;

    private boolean fitsOpaqueNavigationBar = true;

    protected Style(Context context) {
        this.context = context;
        statusBarColor = AppUtils.fetchContextColor(context, android.R.attr.statusBarColor);
        toolbarBackgroundColor = AppUtils.fetchContextColor(context, R.attr.colorPrimary);
        toolbarHeight = AppUtils.fetchContextDimension(context, R.attr.actionBarSize);
        boolean isLightStyle = AppUtils.isDark(toolbarBackgroundColor);
        statusBarStyle = isLightStyle ? BarStyle.LightContent : BarStyle.DarkContent;
    }

    // ------ cutout ------

    public boolean isDisplayCutoutWhenLandscape() {
        return displayCutoutWhenLandscape;
    }

    public void setDisplayCutoutWhenLandscape(boolean displayCutoutWhenLandscape) {
        this.displayCutoutWhenLandscape = displayCutoutWhenLandscape;
    }

    // ------ screen ------

    public void setScreenBackgroundColor(int color) {
        screenBackgroundColor = color;
    }

    public int getScreenBackgroundColor() {
        return screenBackgroundColor;
    }

    // ----- tabBar  -----


    public void setTabBarBackgroundColor(String tabBarBackgroundColor) {
        this.tabBarBackgroundColor = tabBarBackgroundColor;
    }

    public String getTabBarBackgroundColor() {
        return tabBarBackgroundColor;
    }

    public void setTabBarItemColor(String tabBarItemColor) {
        this.tabBarItemColor = tabBarItemColor;
    }

    public String getTabBarItemColor() {
        return tabBarItemColor;
    }

    public String getTabBarUnselectedItemColor() {
        return tabBarUnselectedItemColor;
    }

    public void setTabBarUnselectedItemColor(String tabBarUnselectedItemColor) {
        this.tabBarUnselectedItemColor = tabBarUnselectedItemColor;
    }

    // ------- toolbar ---------

    public int getToolbarHeight() {
        return toolbarHeight;
    }

    public void setToolbarHeight(int toolbarHeight) {
        this.toolbarHeight = toolbarHeight;
    }

    public void setStatusBarStyle(BarStyle barStyle) {
        statusBarStyle = barStyle;
    }

    public BarStyle getStatusBarStyle() {
        return statusBarStyle;
    }

    public void setStatusBarColor(int color) {
        statusBarColor = color;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public boolean isStatusBarColorAnimated() {
        return statusBarColorAnimated;
    }

    public void setStatusBarColorAnimated(boolean statusBarColorAnimated) {
        this.statusBarColorAnimated = statusBarColorAnimated;
    }

    public boolean isStatusBarHidden() {
        return statusBarHidden;
    }

    public void setStatusBarHidden(boolean statusBarHidden) {
        this.statusBarHidden = statusBarHidden;
    }

    public void setToolbarBackgroundColor(int color) {
        toolbarBackgroundColor = color;
        toolbarBackgroundColorDarkContent = color;
        toolbarBackgroundColorLightContent = color;
    }

    public int getToolbarBackgroundColor() {
        return toolbarBackgroundColor;
    }

    public void setToolbarBackgroundColorDarkContent(int toolbarBackgroundColorDarkContent) {
        this.toolbarBackgroundColorDarkContent = toolbarBackgroundColorDarkContent;
    }

    public int getToolbarBackgroundColorDarkContent() {
        return toolbarBackgroundColorDarkContent;
    }

    public void setToolbarBackgroundColorLightContent(int toolbarBackgroundColorLightContent) {
        this.toolbarBackgroundColorLightContent = toolbarBackgroundColorLightContent;
    }

    public int getToolbarBackgroundColorLightContent() {
        return toolbarBackgroundColorLightContent;
    }

    public void setElevation(int dp) {
        this.elevation = AppUtils.dp2px(context, dp);
    }

    public int getElevation() {
        if (elevation != -1) {
            return elevation;
        }
        return AppUtils.dp2px(context, 1);
    }

    public void setToolbarTintColor(int color) {
        toolbarTintColor = color;
        toolbarTintColorDarkContent = color;
        toolbarTintColorLightContent = color;
    }

    public int getToolbarTintColor() {
        return toolbarTintColor;
    }

    public void setToolbarTintColorDarkContent(int toolbarTintColorDarkContent) {
        this.toolbarTintColorDarkContent = toolbarTintColorDarkContent;
    }

    public int getToolbarTintColorDarkContent() {
        return toolbarTintColorDarkContent;
    }

    public void setToolbarTintColorLightContent(int toolbarTintColorLightContent) {
        this.toolbarTintColorLightContent = toolbarTintColorLightContent;
    }

    public int getToolbarTintColorLightContent() {
        return toolbarTintColorLightContent;
    }

    public void setBackIcon(Drawable icon) {
        backIcon = icon;
    }

    public Drawable getBackIcon() {
        if (backIcon == null) {
            backIcon = getDefaultBackIcon();
        }

        if (backIcon.getConstantState() != null) {
            return DrawableCompat.wrap(AppUtils.copyDrawable(backIcon)).mutate();
        } else {
            return DrawableCompat.wrap(backIcon).mutate();
        }
    }

    @NonNull
    private Drawable getDefaultBackIcon() {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.nav_ic_arrow_back);
        if (drawable == null) {
            throw new NullPointerException("should not happen");
        }
        return drawable;
    }

    public void setTitleTextColor(int color) {
        titleTextColor = color;
        titleTextColorDarkContent = color;
        titleTextColorLightContent = color;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColorDarkContent(int titleTextColorDarkContent) {
        this.titleTextColorDarkContent = titleTextColorDarkContent;
    }

    public int getTitleTextColorDarkContent() {
        return titleTextColorDarkContent;
    }

    public void setTitleTextColorLightContent(int titleTextColorLightContent) {
        this.titleTextColorLightContent = titleTextColorLightContent;
    }

    public int getTitleTextColorLightContent() {
        return titleTextColorLightContent;
    }

    public void setTitleTextSize(int dp) {
        titleTextSize = dp;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public float getToolbarAlpha() {
        return toolbarAlpha;
    }

    public void setToolbarAlpha(float toolbarAlpha) {
        this.toolbarAlpha = toolbarAlpha;
    }

    public boolean isToolbarShadowHidden() {
        return toolbarShadowHidden;
    }

    public void setToolbarShadowHidden(boolean toolbarShadowHidden) {
        this.toolbarShadowHidden = toolbarShadowHidden;
    }

    public Drawable getTabBarShadow() {
        return tabBarShadow;
    }

    public void setTabBarShadow(Drawable drawable) {
        this.tabBarShadow = drawable;
    }

    public void setToolbarButtonTextSize(int dp) {
        toolbarButtonTextSize = dp;
    }

    public int getToolbarButtonTextSize() {
        return toolbarButtonTextSize;
    }

    public void setTitleGravity(int gravity) {
        titleGravity = gravity;
    }

    public int getTitleGravity() {
        return titleGravity;
    }

    public boolean isSwipeBackEnabled() {
        return swipeBackEnabled;
    }

    public void setSwipeBackEnabled(boolean swipeBackEnabled) {
        this.swipeBackEnabled = swipeBackEnabled;
    }

    public String getTabBarBadgeColor() {
        return tabBarBadgeColor;
    }

    public void setTabBarBadgeColor(String badgeColor) {
        this.tabBarBadgeColor = badgeColor;
    }

    @ColorInt
    public int getNavigationBarColor() {
        return navigationBarColor;
    }

    public void setNavigationBarColor(@ColorInt int color) {
        this.navigationBarColor = color;
    }

    public boolean isNavigationBarHidden() {
        return navigationBarHidden;
    }

    public void setNavigationBarHidden(boolean hidden) {
        this.navigationBarHidden = hidden;
    }

    public boolean shouldFitsOpaqueNavigationBar() {
        return fitsOpaqueNavigationBar;
    }

    public void setFitsOpaqueNavigationBar(boolean fitsOpaqueNavigationBar) {
        this.fitsOpaqueNavigationBar = fitsOpaqueNavigationBar;
    }

    public void setScrimAlpha(@IntRange(from = 0, to = 255) int scrimAlpha) {
        this.scrimAlpha = scrimAlpha;
    }

    public int getScrimAlpha() {
        return scrimAlpha;
    }

    @NonNull
    @Override
    public Style clone() throws CloneNotSupportedException {
        return (Style) super.clone();
    }
}
