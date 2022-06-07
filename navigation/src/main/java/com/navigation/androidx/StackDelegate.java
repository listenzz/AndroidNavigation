package com.navigation.androidx;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.ValueAnimator;
import android.content.Context;
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
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class StackDelegate {

    private final AwesomeFragment mFragment;

    public StackDelegate(AwesomeFragment fragment) {
        mFragment = fragment;
    }

    private View getView() {
        return mFragment.getView();
    }

    private Style getStyle() {
        return mFragment.mStyle;
    }

    private Context requireContext() {
        return mFragment.requireContext();
    }

    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = mFragment.requireActivity().getLayoutInflater();
        layoutInflater = new StackLayoutInflater(requireContext(), layoutInflater);
        return layoutInflater;
    }

    public StackFragment getStackFragment() {
        if (mFragment instanceof StackFragment) {
            return (StackFragment) mFragment;
        }

        if (mFragment.getShowsDialog()) {
            return null;
        }

        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            return parent.getStackFragment();
        }
        return null;
    }

    public boolean isStackRoot() {
        StackFragment stackFragment = getStackFragment();
        if (stackFragment != null) {
            AwesomeFragment fragment = stackFragment.getRootFragment();
            return fragment == mFragment;
        }
        return false;
    }

    public void setNeedsToolbarAppearanceUpdate() {
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }

        Style style = getStyle();
        setupToolbar(toolbar, style);
        setLeftButtonItemTintColor(style);
        setRightButtonItemTintColor(style);
    }

    private void setRightButtonItemTintColor(Style style) {
        if (mRightBarButtonItems != null) {
            for (ToolbarButtonItem item : mRightBarButtonItems) {
                item.setTintColor(style.getToolbarTintColor());
            }
            return;
        }

        if (mRightBarButtonItem != null) {
            mRightBarButtonItem.setTintColor(style.getToolbarTintColor());
        }
    }

    private void setLeftButtonItemTintColor(Style style) {
        if (mLeftBarButtonItems != null) {
            for (ToolbarButtonItem item : mLeftBarButtonItems) {
                item.setTintColor(style.getToolbarTintColor());
            }
            return;
        }

        if (mLeftBarButtonItem != null) {
            mLeftBarButtonItem.setTintColor(style.getToolbarTintColor());
        }
    }

    public boolean hasStackParent() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        return (parent instanceof StackFragment);
    }

    public void fitStackFragment(@NonNull View root) {
        createToolbar(root);
        fitTabBar(root);
    }

    private AwesomeToolbar mToolbar;

    public AwesomeToolbar getToolbar() {
        return mToolbar;
    }

    private void createToolbar(@NonNull View root) {
        AwesomeToolbar toolbar = mFragment.onCreateToolbar(root);
        if (toolbar != null) {
            setupToolbar(toolbar, getStyle());
        }
        mToolbar = toolbar;
    }

    public AwesomeToolbar onCreateToolbar(View parent) {
        if (getView() == null) return null;
        int toolbarHeight = mFragment.getToolbarHeight();
        AwesomeToolbar toolbar = new AwesomeToolbar(requireContext());
        FrameLayout frameLayout = (FrameLayout) parent;
        frameLayout.addView(toolbar, new FrameLayout.LayoutParams(MATCH_PARENT, toolbarHeight));
        mFragment.appendStatusBarPadding(toolbar);
        appendToolbarPadding(parent);
        return toolbar;
    }

    private void appendToolbarPadding(View parent) {
        if (mFragment.extendedLayoutIncludesToolbar()) {
            return;
        }

        FrameLayout frameLayout = (FrameLayout) parent;
        if (frameLayout.getChildCount() == 0) {
            return;
        }

        View child = frameLayout.getChildAt(0);
        int statusBarHeight = SystemUI.getStatusBarHeight(requireContext());
        int toolbarHeight = mFragment.getToolbarHeight();
        child.setPadding(0, statusBarHeight + toolbarHeight, 0, 0);
    }

    private void fitTabBar(final View root) {
        int index = FragmentHelper.getIndexAtBackStack(mFragment);
        if (index != 0 && shouldHideTabBarWhenPushed()) {
            return;
        }

        int color = Color.parseColor(getStyle().getTabBarBackgroundColor());
        if (Color.alpha(color) != 255) {
            return;
        }

        TabBarFragment tabBarFragment = mFragment.getTabBarFragment();
        if (tabBarFragment == null || tabBarFragment.getTabBar() == null) {
            return;
        }

        int bottomPadding = (int) mFragment.getResources().getDimension(R.dimen.nav_tab_bar_height);
        root.setPadding(0, 0, 0, bottomPadding);
    }

    private boolean shouldHideTabBarWhenPushed() {
        StackFragment stackFragment = getStackFragment();
        AwesomeFragment root = stackFragment.getRootFragment();
        if (root != null && root.isAdded()) {
            return root.hideTabBarWhenPushed();
        }
        return true;
    }

    private void setupToolbar(AwesomeToolbar toolbar, Style style) {
        toolbar.setBackgroundColor(style.getToolbarBackgroundColor());
        toolbar.setButtonTintColor(style.getToolbarTintColor());
        toolbar.setButtonTextSize(style.getToolbarButtonTextSize());
        toolbar.setTitleTextColor(style.getTitleTextColor());
        toolbar.setTitleTextSize(style.getTitleTextSize());
        toolbar.setTitleGravity(style.getTitleGravity());
        if (style.isToolbarShadowHidden()) {
            toolbar.hideShadow();
        } else {
            toolbar.showShadow(style.getElevation());
        }
        toolbar.setAlpha(style.getToolbarAlpha());

        setToolbarBackButton(toolbar, style);
    }

    private void setToolbarBackButton(AwesomeToolbar toolbar, Style style) {
        if (isStackRoot()) {
            return;
        }

        if (mLeftBarButtonItem != null || mLeftBarButtonItems != null) {
            return;
        }

        if (mFragment.shouldHideBackButton()) {
            toolbar.setNavigationIcon(null);
            toolbar.setNavigationOnClickListener(null);
            return;
        }

        toolbar.setNavigationIcon(style.getBackIcon());
        toolbar.setNavigationOnClickListener(view -> {
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                stackFragment.dispatchBackPressed();
            }
        });
    }

    public void setTitle(Context context, @StringRes int resId) {
        setTitle(context.getText(resId));
    }

    public void setTitle(CharSequence title) {
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }
        toolbar.setAwesomeTitle(title);
    }

    private ToolbarButtonItem[] mLeftBarButtonItems;

    public void setLeftBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mLeftBarButtonItems = barButtonItems;
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }

        toolbar.clearLeftButtons();

        if (barButtonItems == null) {
            setToolbarBackButton(toolbar, getStyle());
            return;
        }

        for (ToolbarButtonItem barButtonItem : barButtonItems) {
            toolbar.addLeftButton(barButtonItem);
        }
    }

    @Nullable
    public ToolbarButtonItem[] getLeftBarButtonItems() {
        return mLeftBarButtonItems;
    }

    private ToolbarButtonItem[] mRightBarButtonItems;

    void setRightBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mRightBarButtonItems = barButtonItems;
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }
        toolbar.clearRightButtons();
        if (barButtonItems == null) {
            return;
        }
        for (ToolbarButtonItem barButtonItem : barButtonItems) {
            toolbar.addRightButton(barButtonItem);
        }
    }

    @Nullable
    ToolbarButtonItem[] getRightBarButtonItems() {
        return mRightBarButtonItems;
    }

    private ToolbarButtonItem mLeftBarButtonItem;

    void setLeftBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mLeftBarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }

        toolbar.clearLeftButton();

        if (barButtonItem == null) {
            setToolbarBackButton(toolbar, getStyle());
            return;
        }
        toolbar.setLeftButton(barButtonItem);
        barButtonItem.attach(toolbar.getLeftButton());
    }

    ToolbarButtonItem getLeftBarButtonItem() {
        return mLeftBarButtonItem;
    }

    private ToolbarButtonItem mRightBarButtonItem;

    void setRightBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mRightBarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getToolbar();
        if (toolbar == null) {
            return;
        }
        toolbar.clearRightButton();
        if (barButtonItem == null) {
            return;
        }
        toolbar.setRightButton(barButtonItem);
        barButtonItem.attach(toolbar.getRightButton());
    }

    public ToolbarButtonItem getRightBarButtonItem() {
        return mRightBarButtonItem;
    }

    boolean drawTabBarIfNeeded(int transit, boolean enter, Animation anim) {
        Fragment parent = mFragment.getParentFragment();
        if (!(parent instanceof StackFragment)) {
            return false;
        }

        StackFragment stackFragment = (StackFragment) parent;
        TabBarFragment tabBarFragment = stackFragment.getTabBarFragment();

        if (tabBarFragment == null) {
            return false;
        }

        if (stackFragment != tabBarFragment.getSelectedFragment()) {
            return false;
        }

        if (!shouldHideTabBarWhenPushed()) {
            return false;
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN && !enter) {
            if (mFragment == stackFragment.getRootFragment()) {
                drawTabBar(tabBarFragment, anim.getDuration(), true);
                tabBarFragment.hideTabBarAnimated(anim);
                return true;
            }
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
            if (mFragment == stackFragment.getRootFragment()) {
                drawTabBar(tabBarFragment, anim.getDuration(), false);
                tabBarFragment.showTabBarAnimated(anim);
                return true;
            }
        }

        return false;
    }

    void drawScrimIfNeeded(int transit, boolean enter, Animation anim) {
        Fragment parent = mFragment.getParentFragment();
        if (!(parent instanceof StackFragment)) {
            return;
        }

        StackFragment stackFragment = (StackFragment) parent;
        TransitionAnimation animation = mFragment.getAnimation();
        if (animation.exit == animation.popEnter) {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && !enter) {
                if (getView() != null) {
                    ViewCompat.setTranslationZ(getView(), -1f);
                }
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
        if (stackFragment.getView() == null) {
            return;
        }

        int vWidth = stackFragment.getView().getWidth();
        int vHeight = stackFragment.getView().getHeight();

        ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
        colorDrawable.setBounds(0, 0, vWidth, vHeight);

        View root = getView();
        if (!(root instanceof FrameLayout)) {
            return;
        }

        FrameLayout frameLayout = (FrameLayout) root;
        frameLayout.setForeground(colorDrawable);
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
                frameLayout.setForeground(null);
            }
        }, duration);
    }

    private void drawTabBar(@NonNull TabBarFragment tabBarFragment, long duration, boolean open) {
        if (tabBarFragment.getTabBar() == null || tabBarFragment.getView() == null) {
            return;
        }

        int vWidth = tabBarFragment.getView().getWidth();
        int vHeight = tabBarFragment.getView().getHeight();

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

        View root = getView();
        if (!(root instanceof FrameLayout)) {
            return;
        }

        FrameLayout frameLayout = (FrameLayout) root;
        frameLayout.setForeground(layerDrawable);
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
                frameLayout.setForeground(null);
            }
        }, duration);

    }
}
