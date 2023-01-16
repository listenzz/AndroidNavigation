package com.navigation.androidx;

import static com.navigation.androidx.Style.INVALID_COLOR;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;

public class StackDelegate {

    private final AwesomeFragment mFragment;

    public StackDelegate(AwesomeFragment fragment) {
        mFragment = fragment;
    }

    public Window getWindow() {
        return mFragment.getWindow();
    }

    private FrameLayout requireView() {
        return (FrameLayout) mFragment.requireView();
    }

    private Style getStyle() {
        return mFragment.mStyle;
    }

    private Context requireContext() {
        return mFragment.requireContext();
    }

    public LayoutInflater onGetLayoutInflater(LayoutInflater layoutInflater, @Nullable Bundle savedInstanceState) {
        return new StackLayoutInflater(requireContext(), layoutInflater);
    }

    public boolean isStackRoot() {
        if (!hasStackParent()) {
            return false;
        }
        StackFragment stackFragment = mFragment.requireStackFragment();
        return mFragment == stackFragment.getRootFragment();
    }

    public boolean hasStackParent() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        return (parent instanceof StackFragment);
    }

    private AwesomeToolbar mToolbar;

    public AwesomeToolbar getToolbar() {
        return mToolbar;
    }

    public void createToolbar() {
        mToolbar = mFragment.onCreateToolbar(requireView());
        if (mToolbar != null) {
            renderToolbar(mToolbar);
        }
    }

    public void renderToolbar(AwesomeToolbar toolbar) {
        Style style = getStyle();
        toolbar.setBackgroundColor(getToolbarBackgroundColor());
        toolbar.setButtonTintColor(getToolbarTintColor());
        toolbar.setButtonTextSize(style.getToolbarButtonTextSize());
        toolbar.setTitleTextColor(getTitleTextColor());
        toolbar.setTitleTextSize(style.getTitleTextSize());
        toolbar.setTitleGravity(style.getTitleGravity());
        if (style.isToolbarShadowHidden()) {
            toolbar.hideShadow();
        } else {
            toolbar.showShadow(style.getElevation());
        }
        toolbar.setAlpha(style.getToolbarAlpha());

        setToolbarBackButton();
    }

    void fitsStatusBar() {
        if (!hasStackParent()) {
            return;
        }

        AwesomeToolbar toolbar = getToolbar();
        FrameLayout root = requireView();
        if (toolbar != null && root.getChildAt(1) == toolbar) {
            fitsToolbar(toolbar);
            fitsWrappedView();
        }
    }

    public void fitsToolbar(@NonNull AwesomeToolbar toolbar) {
        EdgeInsets edge = SystemUI.getEdgeInsetsForView(toolbar);
        if (edge.top > 0) {
            return;
        }

        int statusBarHeight = SystemUI.statusBarHeight(getWindow());
        ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        if (lp != null && lp.height > 0) {
            lp.height += statusBarHeight;
        }
        toolbar.setPadding(0, statusBarHeight, 0, 0);
    }

    private void fitsWrappedView() {
        if (mFragment.extendedLayoutIncludesToolbar()) {
            return;
        }
        View root = getWrappedView();
        if (root == null) {
            return;
        }
        int statusBarHeight = SystemUI.statusBarHeight(getWindow());
        root.setPadding(0, statusBarHeight + getToolbarHeight(), 0, 0);
    }

    private View getWrappedView() {
        FrameLayout frameLayout = (FrameLayout) requireView();
        if (frameLayout.getChildCount() == 0) {
            return null;
        }
        return frameLayout.getChildAt(0);
    }

    void fitsTabBarIfNeeded() {
        if (!hasStackParent()) {
            return;
        }

        StackFragment stackFragment = mFragment.requireStackFragment();
        TabBarFragment tabBarFragment = stackFragment.getTabBarFragment();
        if (tabBarFragment == null) {
            return;
        }

        if (mFragment == stackFragment.getRootFragment() || stackFragment.shouldShowTabBarWhenPushed()) {
            fitsTabBar(tabBarFragment);
        }
    }

    private void fitsTabBar(TabBarFragment tabBarFragment) {
        int bottomPadding = (int) mFragment.getResources().getDimension(R.dimen.nav_tab_bar_height);
        if (tabBarFragment.shouldFitsNavigationBar()) {
            requireView().setPadding(0, 0, 0, bottomPadding + SystemUI.navigationBarHeight(getWindow()));
        } else {
            requireView().setPadding(0, 0, 0, bottomPadding);
        }
    }

    public int getToolbarHeight() {
        return getStyle().getToolbarHeight();
    }

    public int getToolbarBackgroundColor() {
        BarStyle barStyle = mFragment.preferredStatusBarStyle();
        Style style = getStyle();

        if (barStyle == BarStyle.DarkContent && style.getToolbarBackgroundColorDarkContent() != INVALID_COLOR) {
            return style.getToolbarBackgroundColorDarkContent();
        }

        if (barStyle == BarStyle.LightContent && style.getToolbarBackgroundColorLightContent() != INVALID_COLOR) {
            return style.getToolbarBackgroundColorLightContent();
        }

        if (style.getToolbarBackgroundColor() != INVALID_COLOR) {
            return style.getToolbarBackgroundColor();
        }

        if (barStyle == BarStyle.LightContent) {
            return Color.BLACK;
        }

        return Color.WHITE;
    }

    private int getToolbarTintColor() {
        BarStyle barStyle = mFragment.preferredStatusBarStyle();
        Style style = getStyle();

        if (barStyle == BarStyle.DarkContent && style.getToolbarTintColorDarkContent() != INVALID_COLOR) {
            return style.getToolbarTintColorDarkContent();
        }

        if (barStyle == BarStyle.LightContent && style.getToolbarTintColorLightContent() != INVALID_COLOR) {
            return style.getToolbarTintColorLightContent();
        }

        if (style.getToolbarTintColor() != INVALID_COLOR) {
            return style.getToolbarTintColor();
        }

        if (barStyle == BarStyle.LightContent) {
            return Color.WHITE;
        }

        return Color.parseColor("#131940");

    }

    public int getTitleTextColor() {
        BarStyle barStyle = mFragment.preferredStatusBarStyle();
        Style style = getStyle();

        if (barStyle == BarStyle.DarkContent && style.getTitleTextColorDarkContent() != INVALID_COLOR) {
            return style.getTitleTextColorDarkContent();
        }

        if (barStyle == BarStyle.LightContent && style.getTitleTextColorLightContent() != INVALID_COLOR) {
            return style.getTitleTextColorLightContent();
        }

        if (style.getTitleTextColor() != INVALID_COLOR) {
            return style.getTitleTextColor();
        }

        if (barStyle == BarStyle.LightContent) {
            return Color.WHITE;
        }

        return Color.parseColor("#131940");
    }

    private void setToolbarBackButton() {
        if (isStackRoot()) {
            return;
        }

        if (mLeftBarButtonItem != null || mLeftBarButtonItems != null) {
            return;
        }

        AwesomeToolbar toolbar = mToolbar;

        if (mFragment.shouldHideBackButton()) {
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
        } else {
            toolbar.setNavigationIcon(getBackIcon());
            toolbar.setNavigationOnClickListener(view -> {
                StackFragment stackFragment = mFragment.requireStackFragment();
                stackFragment.dispatchBackPressed();
            });
        }
    }

    private Drawable getBackIcon() {
        Style style = getStyle();
        Drawable icon = style.getBackIcon();
        icon.setTintList(getBackIconTintList());
        return icon;
    }

    private ColorStateList getBackIconTintList() {
        return AppUtils.buttonColorStateList(getToolbarTintColor());
    }

    public void setNeedsToolbarAppearanceUpdate() {
        if (mToolbar == null) {
            return;
        }

        renderToolbar(getToolbar());
        setLeftButtonItemTintColor();
        setRightButtonItemTintColor();
    }

    private void setLeftButtonItemTintColor() {
        if (mLeftBarButtonItems != null) {
            for (ToolbarButtonItem item : mLeftBarButtonItems) {
                item.setTintColor(getToolbarTintColor());
            }
            return;
        }

        if (mLeftBarButtonItem != null) {
            mLeftBarButtonItem.setTintColor(getToolbarTintColor());
        }
    }

    private void setRightButtonItemTintColor() {
        if (mRightBarButtonItems != null) {
            for (ToolbarButtonItem item : mRightBarButtonItems) {
                item.setTintColor(getToolbarTintColor());
            }
            return;
        }

        if (mRightBarButtonItem != null) {
            mRightBarButtonItem.setTintColor(getToolbarTintColor());
        }
    }

    public void setTitle(Context context, @StringRes int resId) {
        setTitle(context.getText(resId));
    }

    public void setTitle(CharSequence title) {
        if (mToolbar != null) {
            mToolbar.setAwesomeTitle(title);
        }
    }

    private ToolbarButtonItem[] mLeftBarButtonItems;

    public void setLeftBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mLeftBarButtonItems = barButtonItems;
        if (mToolbar == null) {
            return;
        }

        mToolbar.clearLeftButtons();

        if (barButtonItems == null) {
            setToolbarBackButton();
            return;
        }

        for (ToolbarButtonItem barButtonItem : barButtonItems) {
            mToolbar.addLeftButton(barButtonItem);
        }
    }

    @Nullable
    public ToolbarButtonItem[] getLeftBarButtonItems() {
        return mLeftBarButtonItems;
    }

    private ToolbarButtonItem[] mRightBarButtonItems;

    void setRightBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mRightBarButtonItems = barButtonItems;
        if (mToolbar == null) {
            return;
        }

        mToolbar.clearRightButtons();
        if (barButtonItems == null) {
            return;
        }

        for (ToolbarButtonItem barButtonItem : barButtonItems) {
            mToolbar.addRightButton(barButtonItem);
        }
    }

    @Nullable
    ToolbarButtonItem[] getRightBarButtonItems() {
        return mRightBarButtonItems;
    }

    private ToolbarButtonItem mLeftBarButtonItem;

    void setLeftBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mLeftBarButtonItem = barButtonItem;
        if (mToolbar == null) {
            return;
        }

        mToolbar.clearLeftButton();

        if (barButtonItem == null) {
            setToolbarBackButton();
            return;
        }

        mToolbar.setLeftButton(barButtonItem);
        barButtonItem.attach(mToolbar.getLeftButton());
    }

    ToolbarButtonItem getLeftBarButtonItem() {
        return mLeftBarButtonItem;
    }

    private ToolbarButtonItem mRightBarButtonItem;

    void setRightBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mRightBarButtonItem = barButtonItem;
        if (mToolbar == null) {
            return;
        }

        mToolbar.clearRightButton();

        if (barButtonItem == null) {
            return;
        }

        mToolbar.setRightButton(barButtonItem);
        barButtonItem.attach(mToolbar.getRightButton());
    }

    public ToolbarButtonItem getRightBarButtonItem() {
        return mRightBarButtonItem;
    }

    boolean drawTabBarIfNeeded(int transit, boolean enter, Animation anim) {
        if (!hasStackParent()) {
            return false;
        }

        StackFragment stackFragment = mFragment.requireStackFragment();
        TabBarFragment tabBarFragment = stackFragment.getTabBarFragment();

        if (tabBarFragment == null || tabBarFragment.getSelectedFragment() != stackFragment) {
            return false;
        }

        if (mFragment != stackFragment.getRootFragment() || stackFragment.shouldShowTabBarWhenPushed()) {
            return false;
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN && !enter) {
            drawTabBar(tabBarFragment, anim.getDuration(), true);
            tabBarFragment.hideTabBarAnimated(anim);
            return true;
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
            drawTabBar(tabBarFragment, anim.getDuration(), false);
            tabBarFragment.showTabBarAnimated(anim);
            return true;
        }

        return false;
    }

    private void drawTabBar(@NonNull TabBarFragment tabBarFragment, long duration, boolean open) {
        int vWidth = tabBarFragment.requireView().getWidth();
        int vHeight = tabBarFragment.requireView().getHeight();

        View tabBar = tabBarFragment.getTabBar();
        if (tabBar.getMeasuredWidth() == 0) {
            tabBar.measure(View.MeasureSpec.makeMeasureSpec(vWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            tabBar.layout(0, 0, tabBar.getMeasuredWidth(), tabBar.getMeasuredHeight());
        }

        Bitmap bitmap = AppUtils.createBitmapFromView(tabBar);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(mFragment.getResources(), bitmap);
        bitmapDrawable.setBounds(0, vHeight - tabBar.getHeight(),
                tabBar.getMeasuredWidth(), vHeight);
        bitmapDrawable.setGravity(Gravity.BOTTOM);

        // scrim
        ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
        colorDrawable.setBounds(0, 0, vWidth, vHeight);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{bitmapDrawable, colorDrawable,});
        layerDrawable.setBounds(0, 0, vWidth, vHeight);

        FrameLayout root = (FrameLayout) requireView();
        root.setForeground(layerDrawable);
        int scrimAlpha = getStyle().getScrimAlpha();
        ValueAnimator valueAnimator = open ? ValueAnimator.ofInt(0, scrimAlpha) : ValueAnimator.ofInt(scrimAlpha, 0);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            colorDrawable.setColor(value << 24);
        });
        valueAnimator.start();

        root.postDelayed(() -> {
            if (mFragment.isAdded()) {
                root.setForeground(null);
            }
        }, duration + 8);

    }

    void drawScrimIfNeeded(int transit, boolean enter, Animation anim) {
        if (!hasStackParent()) {
            return;
        }
        StackFragment stackFragment = mFragment.requireStackFragment();

        TransitionAnimation animation = mFragment.getAnimation();
        if (animation.exit == animation.popEnter) {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && !enter) {
                ViewCompat.setTranslationZ(requireView(), -1f);
                drawScrim(stackFragment, anim.getDuration(), true);
            }
        } else {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN && !enter) {
                drawScrim(stackFragment, anim.getDuration(), true);
            } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                drawScrim(stackFragment, anim.getDuration(), false);
            }
        }
    }

    private void drawScrim(@NonNull StackFragment stackFragment, long duration, boolean open) {
        int vWidth = stackFragment.requireView().getWidth();
        int vHeight = stackFragment.requireView().getHeight();

        ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
        colorDrawable.setBounds(0, 0, vWidth, vHeight);

        FrameLayout root = requireView();
        root.setForeground(colorDrawable);
        int scrimAlpha = getStyle().getScrimAlpha();
        ValueAnimator valueAnimator = open ? ValueAnimator.ofInt(0, scrimAlpha) : ValueAnimator.ofInt(scrimAlpha, 0);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            colorDrawable.setColor(value << 24);
        });
        valueAnimator.start();

        root.postDelayed(() -> {
            if (mFragment.isAdded()) {
                root.setForeground(null);
            }
        }, duration);
    }
}
