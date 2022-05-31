package com.navigation.androidx;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.InternalFragment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Listen on 2018/1/11.
 */

public abstract class AwesomeFragment extends InternalFragment {

    public static final String TAG = "Navigation";

    static final String ARGS_REQUEST_CODE = "nav_request_code";
    static final String ARGS_SHOW_AS_DIALOG = "show_as_dialog";

    private static final String SAVED_TAB_BAR_ITEM = "nav_tab_bar_item";
    private static final String SAVED_SCENE_ID = "nav_scene_id";


    // ------- lifecycle methods -------
    private PresentableActivity mPresentableActivity;
    private final LifecycleDelegate mLifecycleDelegate = new LifecycleDelegate(this);
    private final PresentationDelegate mPresentationDelegate = new PresentationDelegate(this);
    private final DialogDelegate mDialogDelegate = new DialogDelegate(this);
    private final StackDelegate mStackDelegate = new StackDelegate(this);

    protected Style mStyle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof PresentableActivity)) {
            throw new IllegalArgumentException("Activity must implements PresentableActivity!");
        }
        mPresentableActivity = (PresentableActivity) activity;
        mPresentationDelegate.setPresentableActivity(mPresentableActivity);
        inflateStyle();
    }

    @Override
    public void onDetach() {
        mPresentableActivity = null;
        mPresentationDelegate.setPresentableActivity(null);
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSceneId = savedInstanceState.getString(SAVED_SCENE_ID);
            mTabBarItem = savedInstanceState.getParcelable(SAVED_TAB_BAR_ITEM);
        }

        mPresentationDelegate.onCreate(savedInstanceState);

        Bundle args = FragmentHelper.getArguments(this);
        boolean showAsDialog = args.getBoolean(ARGS_SHOW_AS_DIALOG, false);
        setShowsDialog(showAsDialog);
        setResult(0, null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_SCENE_ID, mSceneId);
        outState.putParcelable(SAVED_TAB_BAR_ITEM, mTabBarItem);
        mPresentationDelegate.onSaveInstanceState(outState);
    }

    @Override
    @NonNull
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        inflateStyle();

        if (getShowsDialog()) {
            setStyle(STYLE_NORMAL, R.style.Theme_Nav_FullScreenDialog);
            super.onGetLayoutInflater(savedInstanceState);
            return mDialogDelegate.onGetLayoutInflater(savedInstanceState);
        }

        if (mStackDelegate.hasStackParent()) {
            return mStackDelegate.onGetLayoutInflater(savedInstanceState);
        }
        return super.onGetLayoutInflater(savedInstanceState);
    }

    private void inflateStyle() {
        if (mStyle != null) {
            return;
        }
        if (mPresentableActivity == null || mPresentableActivity.getStyle() == null) {
            return;
        }

        try {
            mStyle = mPresentableActivity.getStyle().clone();
            onCustomStyle(mStyle);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            mStyle = mPresentableActivity.getStyle();
        }
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    @Override
    protected void performCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.performCreateView(inflater, container, savedInstanceState);
        View root = getView();
        if (root == null) {
            return;
        }

        if (getShowsDialog()) {
            mDialogDelegate.setupDialog();
        }

        if (mStackDelegate.hasStackParent()) {
            mStackDelegate.fitStackFragment(root);
        }

        if (!isParentFragment()) {
            setBackgroundDrawable(root, mStyle.getScreenBackgroundColor());
        }
    }

    @Override
    public void onDestroyView() {
        AppUtils.hideSoftInput(getWindow());
        super.onDestroyView();
    }

    private void setBackgroundDrawable(View root, int color) {
        setBackgroundDrawableForView(root, color);
        setBackgroundDrawableForWindow(color);
    }

    private void setBackgroundDrawableForView(View root, int color) {
        if (getShowsDialog()) {
            return;
        }
        root.setBackground(new ColorDrawable(color));
    }

    private void setBackgroundDrawableForWindow(int color) {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        AwesomeFragment fragment = getDialogFragment();
        if (fragment != null) {
            if (Color.alpha(color) < 255) {
                window.setDimAmount(0);
                window.setBackgroundDrawable(new ColorDrawable(color));
            }
            return;
        }

        window.setBackgroundDrawable(new ColorDrawable(color));
    }

    @Nullable
    public Window getWindow() {
        AwesomeFragment fragment = getDialogFragment();
        if (fragment != null) {
            Dialog dialog = fragment.requireDialog();
            return dialog.getWindow();
        }

        if (getActivity() != null) {
            return getActivity().getWindow();
        }

        return null;
    }

    @Nullable
    public AwesomeFragment getDialogFragment() {
        if (getShowsDialog()) {
            return this;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent == null) {
            return null;
        }

        return parent.getDialogFragment();
    }

    protected boolean isInDialog() {
        return getDialogFragment() != null;
    }

    @Nullable
    public AwesomeFragment getParentAwesomeFragment() {
        if (getShowsDialog()) {
            return null;
        }

        Fragment fragment = getParentFragment();
        if (fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    public boolean isParentFragment() {
        return false;
    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        //Log.i(TAG, getDebugTag() + "#onResume");
        if (childFragmentForAppearance() == null) {
            setNeedsStatusBarAppearanceUpdate();
        }

        if (childFragmentForNavigationBarAppearance() == null) {
            setNeedsNavigationBarAppearanceUpdate();
        }

        if (childFragmentForAppearance() == null) {
            setNeedsLayoutInDisplayCutoutModeUpdate();
        }
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        //Log.i(TAG, getDebugTag() + "#onPause");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (isResumed() && !isParentFragment()) {
            setDisplayCutoutWhenLandscape(newConfig.orientation);
        }
        super.onConfigurationChanged(newConfig);
    }

    public void setNeedsLayoutInDisplayCutoutModeUpdate() {
        setDisplayCutoutWhenLandscape(getResources().getConfiguration().orientation);
    }

    private void setDisplayCutoutWhenLandscape(int orientation) {
        boolean displayCutout = mStyle.isDisplayCutoutWhenLandscape() || orientation == Configuration.ORIENTATION_PORTRAIT;
        SystemUI.setRenderContentInShortEdgeCutoutAreas(getWindow(), displayCutout);
    }

    private TransitionAnimation mAnimation = null;

    public void setAnimation(@Nullable TransitionAnimation animation) {
        mAnimation = animation;
    }

    @NonNull
    public TransitionAnimation getAnimation() {
        if (mAnimation == null) {
            mAnimation = TransitionAnimation.None;
        }
        return mAnimation;
    }

    @Override
    @CallSuper
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        // Log.i(TAG, getDebugTag() + "  " + " transit:" + transit + " enter:" + enter + " nextAnim:" + nextAnim + " isAdd:" + isAdded() + " inRemoving:" + isRemoving());

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && FragmentHelper.isRemoving(parent)) {
            return AnimationUtils.loadAnimation(context, R.anim.nav_delay);
        }

        TransitionAnimation animation = getAnimation();

        Animation anim = null;
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                anim = AnimationUtils.loadAnimation(context, nextAnim == 0 ? animation.enter : nextAnim);
            } else {
                anim = AnimationUtils.loadAnimation(context, nextAnim == 0 ? animation.exit : nextAnim);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                anim = AnimationUtils.loadAnimation(context, nextAnim == 0 ? animation.popEnter : nextAnim);
            } else {
                anim = AnimationUtils.loadAnimation(context, nextAnim == 0 ? animation.popExit : nextAnim);
            }
        }

        if (!mStackDelegate.drawTabBarIfNeeded(transit, enter, anim)) {
            mStackDelegate.drawScrimIfNeeded(transit, enter, anim);
        }

        return anim;
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        mLifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

    // ------- presentation ------

    private String mSceneId;

    @NonNull
    public String getSceneId() {
        if (mSceneId == null) {
            mSceneId = UUID.randomUUID().toString();
        }
        return mSceneId;
    }

    public void setDefinesPresentationContext(boolean defines) {
        mPresentationDelegate.setDefinesPresentationContext(defines);
    }

    public boolean definesPresentationContext() {
        return mPresentationDelegate.definesPresentationContext();
    }

    protected boolean dispatchBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();

        if (fragment instanceof AwesomeFragment && ((AwesomeFragment) fragment).definesPresentationContext() && count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            if (child != null) {
                boolean processed = child.dispatchBackPressed() || onBackPressed();
                if (!processed) {
                    child.dismissFragment(null);
                }
                return true;
            }
        }

        if (fragment instanceof AwesomeFragment) {
            AwesomeFragment child = (AwesomeFragment) fragment;
            return child.dispatchBackPressed() || onBackPressed();
        } else if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            return (child != null && child.dispatchBackPressed()) || onBackPressed();
        } else {
            return onBackPressed();
        }
    }

    protected boolean onBackPressed() {
        return false;
    }

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode) {
        presentFragment(fragment, requestCode, null);
    }

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            mPresentationDelegate.presentFragment(fragment, requestCode, completion);
        });
    }

    public void dismissFragment() {
        dismissFragment(null);
    }

    public void dismissFragment(@Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            mPresentationDelegate.dismissFragment(completion);
        });
    }

    @Nullable
    public AwesomeFragment getPresentedFragment() {
        return mPresentationDelegate.getPresentedFragment();
    }

    @Nullable
    public AwesomeFragment getPresentingFragment() {
        return mPresentationDelegate.getPresentingFragment();
    }

    public void setActivityRootFragment(AwesomeFragment root) {
        if (mPresentableActivity != null) {
            mPresentableActivity.setActivityRootFragment(root);
        }
    }

    private int mRequestCode;
    private int mResultCode;
    private Bundle mResult;

    public void setResult(int resultCode, Bundle data) {
        mResult = data;
        mResultCode = resultCode;
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && !definesPresentationContext()) {
            parent.setResult(resultCode, data);
        }
    }

    public int getRequestCode() {
        if (mRequestCode == 0) {
            Bundle args = FragmentHelper.getArguments(this);
            mRequestCode = args.getInt(ARGS_REQUEST_CODE);
        }
        return mRequestCode;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public Bundle getResultData() {
        return mResult;
    }

    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle data) {
        // Log.i(TAG, toString() + "#onFragmentResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        if (this instanceof TabBarFragment) {
            AwesomeFragment child = ((TabBarFragment) this).getSelectedFragment();
            if (child != null) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        } else if (this instanceof StackFragment) {
            AwesomeFragment child = ((StackFragment) this).getTopFragment();
            if (child != null) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        } else if (this instanceof DrawerFragment) {
            AwesomeFragment child = ((DrawerFragment) this).getContentFragment();
            if (child != null) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        } else {
            List<AwesomeFragment> fragments = getChildFragments();
            for (AwesomeFragment child : fragments) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        }
    }

    public String getDebugTag() {
        if (getActivity() == null) {
            return null;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent == null) {
            return "#" + FragmentHelper.indexOf(this) + "-" + getClass().getSimpleName();
        } else {
            return parent.getDebugTag() + "#" + FragmentHelper.indexOf(this) + "-" + getClass().getSimpleName();
        }
    }

    @NonNull
    public List<AwesomeFragment> getChildFragments() {
        if (isAdded()) {
            return FragmentHelper.getFragments(getChildFragmentManager());
        }
        return Collections.emptyList();
    }

    // ------- statusBar --------

    @Nullable
    protected AwesomeFragment childFragmentForAppearance() {
        return null;
    }

    private AwesomeFragment fragmentForStatusBarAppearance() {
        AwesomeFragment childFragment = childFragmentForAppearance();
        if (childFragment == null) {
            return this;
        } else {
            return childFragment.fragmentForStatusBarAppearance();
        }
    }

    @Nullable
    protected AwesomeFragment childFragmentForStatusBarHidden() {
        return childFragmentForAppearance();
    }

    private AwesomeFragment fragmentForStatusBarHidden() {
        AwesomeFragment childFragment = childFragmentForStatusBarHidden();
        if (childFragment == null) {
            return this;
        } else {
            return childFragment.fragmentForStatusBarHidden();
        }
    }

    @NonNull
    protected BarStyle preferredStatusBarStyle() {
        if (getShowsDialog()) {
            return BarStyle.LightContent;
        }
        return mStyle.getStatusBarStyle();
    }

    protected boolean preferredStatusBarHidden() {
        if (getShowsDialog()) {
            return SystemUI.isStatusBarHidden(requireActivity().getWindow());
        }

        return mStyle.isStatusBarHidden();
    }

    protected int preferredStatusBarColor() {
        if (getShowsDialog()) {
            return Color.TRANSPARENT;
        }

        return mStyle.getStatusBarColor();
    }

    protected boolean preferredStatusBarColorAnimated() {
        return getAnimation() != TransitionAnimation.None && mStyle.isStatusBarColorAnimated();
    }

    public void setNeedsStatusBarAppearanceUpdate() {
        if (!isResumed()) {
            return;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.setNeedsStatusBarAppearanceUpdate();
            return;
        }

        AwesomeFragment fragment = fragmentForStatusBarAppearance();
        if (!fragment.isResumed()) {
            return;
        }

        // statusBarHidden
        boolean hidden = fragmentForStatusBarHidden().preferredStatusBarHidden();
        setStatusBarHidden(hidden);

        // statusBarStyle
        BarStyle statusBarStyle = fragment.preferredStatusBarStyle();
        setStatusBarStyle(statusBarStyle);

        // statusBarColor
        boolean animated = fragment.preferredStatusBarColorAnimated();
        int statusBarColor = fragment.preferredStatusBarColor();
        setStatusBarColor(statusBarColor, animated);
    }

    private void setStatusBarStyle(BarStyle barStyle) {
        SystemUI.setStatusBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    private void setStatusBarHidden(boolean hidden) {
        Window window = getWindow();
        if (window != null) {
            SystemUI.setStatusBarHidden(getWindow(), hidden);
        }
    }

    private void setStatusBarColor(int color, boolean animated) {
        Window window = getWindow();
        if (window != null) {
            SystemUI.setStatusBarColor(getWindow(), color, animated);
        }
    }

    public void appendStatusBarPadding(View view) {
        SystemUI.appendStatusBarPadding(requireContext(), view);
    }

    public void removeStatusBarPadding(View view) {
        SystemUI.removeStatusBarPadding(requireContext(), view);
    }

    // ------- NavigationBar --------

    @Nullable
    protected AwesomeFragment childFragmentForNavigationBarAppearance() {
        return childFragmentForAppearance();
    }

    private AwesomeFragment fragmentForNavigationBarAppearance() {
        AwesomeFragment childFragment = childFragmentForNavigationBarAppearance();
        if (childFragment == null) {
            return this;
        } else {
            return childFragment.fragmentForNavigationBarAppearance();
        }
    }

    @ColorInt
    protected int preferredNavigationBarColor() {
        if (getShowsDialog()) {
            return mDialogDelegate.preferredNavigationBarColor();
        }

        if (mStyle.getNavigationBarColor() != Style.INVALID_COLOR) {
            return mStyle.getNavigationBarColor();
        } else {
            return mStyle.getScreenBackgroundColor();
        }
    }

    @NonNull
    protected BarStyle preferredNavigationBarStyle() {
        return AppUtils.isBlackColor(preferredNavigationBarColor(), 176) ? BarStyle.LightContent : BarStyle.DarkContent;
    }

    protected boolean preferredNavigationBarHidden() {
        if (getShowsDialog()) {
            return SystemUI.isNavigationBarHidden(requireActivity().getWindow());
        }
        return mStyle.isNavigationBarHidden();
    }

    public void setNeedsNavigationBarAppearanceUpdate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (!isResumed()) {
            return;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.setNeedsNavigationBarAppearanceUpdate();
            return;
        }

        AwesomeFragment fragment = fragmentForNavigationBarAppearance();
        if (!fragment.isResumed()) {
            return;
        }
        setNavigationBarColor(fragment.preferredNavigationBarColor());
        setNavigationBarStyle(fragment.preferredNavigationBarStyle());
        setNavigationBarHidden(fragment.preferredNavigationBarHidden());
        setNavigationBarLayoutHidden(fragment.preferredNavigationBarHidden() ||
                Color.alpha(fragment.preferredNavigationBarColor()) < 255);
    }

    private void setNavigationBarStyle(BarStyle barStyle) {
        SystemUI.setNavigationBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    private void setNavigationBarColor(int color) {
        SystemUI.setNavigationBarColor(getWindow(), color);
    }

    private void setNavigationBarHidden(boolean hidden) {
        SystemUI.setNavigationBarHidden(getWindow(), hidden);
    }

    private void setNavigationBarLayoutHidden(boolean hidden) {
        SystemUI.setNavigationBarLayoutHidden(getWindow(), hidden);
    }

    // ------ dialog -----

    /**
     * @deprecated call {@link #showAsDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    /**
     * @deprecated call {@link #showAsDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public int show(@NonNull FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag);
    }

    /**
     * @deprecated call {@link #hideAsDialog()} instead of this method.
     */
    @Deprecated
    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        showAsDialog(dialog, requestCode, null);
    }

    public void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            mDialogDelegate.showAsDialog(dialog, requestCode, completion);
        });
    }

    /**
     * Dismiss the fragment as dialog.
     */
    public void hideAsDialog() {
        hideAsDialog(null);
    }

    public void hideAsDialog(@Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> mDialogDelegate.hideAsDialog(completion, false));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mDialogDelegate.onDismiss();
    }

    // ------ StackFragment -----

    @NonNull
    public StackFragment requireStackFragment() {
        StackFragment stackFragment = getStackFragment();
        if (stackFragment == null) {
            throw new NullPointerException("StackFragment is null, make sure this fragment is wrapped in A StackFragment.");
        }
        return stackFragment;
    }

    @Nullable
    public StackFragment getStackFragment() {
        return mStackDelegate.getStackFragment();
    }

    public boolean isStackRoot() {
        return mStackDelegate.isStackRoot();
    }

    protected boolean shouldHideBackButton() {
        return false;
    }

    protected boolean isBackInteractive() {
        return true;
    }

    protected boolean isSwipeBackEnabled() {
        return true;
    }

    protected boolean hideTabBarWhenPushed() {
        return true;
    }

    public AwesomeToolbar getToolbar() {
        return mStackDelegate.getToolbar();
    }

    @Nullable
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return mStackDelegate.onCreateToolbar(parent);
    }

    protected boolean extendedLayoutIncludesToolbar() {
        int color = mStyle.getToolbarBackgroundColor();
        float alpha = mStyle.getToolbarAlpha();
        return Color.alpha(color) < 255 || alpha < 1.0;
    }

    public int getToolbarHeight() {
        return mStyle.getToolbarHeight();
    }

    public void setNeedsToolbarAppearanceUpdate() {
        mStackDelegate.setNeedsToolbarAppearanceUpdate();
    }

    public void setTitle(@StringRes int resId) {
        mStackDelegate.setTitle(getContext(), resId);
    }

    public void setTitle(CharSequence title) {
        mStackDelegate.setTitle(title);
    }

    public void setLeftBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mStackDelegate.setLeftBarButtonItems(barButtonItems);
    }

    @Nullable
    public ToolbarButtonItem[] getLeftBarButtonItems() {
        return mStackDelegate.getLeftBarButtonItems();
    }

    public void setRightBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        mStackDelegate.setRightBarButtonItems(barButtonItems);
    }

    @Nullable
    public ToolbarButtonItem[] getRightBarButtonItems() {
        return mStackDelegate.getRightBarButtonItems();
    }

    public void setLeftBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mStackDelegate.setLeftBarButtonItem(barButtonItem);
    }

    @Nullable
    public ToolbarButtonItem getLeftBarButtonItem() {
        return mStackDelegate.getLeftBarButtonItem();
    }

    public void setRightBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        mStackDelegate.setRightBarButtonItem(barButtonItem);
    }

    @Nullable
    public ToolbarButtonItem getRightBarButtonItem() {
        return mStackDelegate.getRightBarButtonItem();
    }

    // ------ TabBarFragment -------

    @NonNull
    public TabBarFragment requireTabBarFragment() {
        TabBarFragment tabBarFragment = getTabBarFragment();
        if (tabBarFragment == null) {
            throw new NullPointerException("TabBarFragment is null, make sure this fragment is wrapped in a TabBarFragment.");
        }
        return tabBarFragment;
    }

    @Nullable
    public TabBarFragment getTabBarFragment() {
        if (this instanceof TabBarFragment) {
            return (TabBarFragment) this;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getTabBarFragment();
        }
        return null;
    }

    private TabBarItem mTabBarItem;

    public void setTabBarItem(@Nullable TabBarItem item) {
        mTabBarItem = item;
    }

    @Nullable
    public TabBarItem getTabBarItem() {
        return mTabBarItem;
    }

    // ------ DrawerFragment -------

    @NonNull
    public DrawerFragment requireDrawerFragment() {
        DrawerFragment drawerFragment = getDrawerFragment();
        if (drawerFragment == null) {
            throw new NullPointerException("DrawerFragment is null, make sure this fragment is wrapped in a DrawerFragment.");
        }
        return drawerFragment;
    }

    @Nullable
    public DrawerFragment getDrawerFragment() {
        if (this instanceof DrawerFragment) {
            return (DrawerFragment) this;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getDrawerFragment();
        }
        return null;
    }

}
