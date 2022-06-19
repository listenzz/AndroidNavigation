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

public abstract class AwesomeFragment extends InternalFragment {

    public static final String TAG = "Navigation";

    static final String ARGS_REQUEST_CODE = "nav_request_code";
    static final String ARGS_SHOW_AS_DIALOG = "show_as_dialog";

    private static final String SAVED_TAB_BAR_ITEM = "nav_tab_bar_item";
    private static final String SAVED_SCENE_ID = "nav_scene_id";
    private static final String SAVED_PRESENTATION_STYLE = "presentation_style";

    private PresentableActivity mPresentableActivity;
    private final LifecycleDelegate mLifecycleDelegate = new LifecycleDelegate(this);
    private final PresentationDelegate mPresentationDelegate = new PresentationDelegate(this);
    private final DialogDelegate mDialogDelegate = new DialogDelegate(this);
    private final StackDelegate mStackDelegate = new StackDelegate(this);

    protected Style mStyle;

    // -------- lifecycle --------

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
            String style = savedInstanceState.getString(SAVED_PRESENTATION_STYLE);
            setPresentationStyle(PresentationStyle.valueOf(style));
        }

        mPresentationDelegate.onCreate(savedInstanceState);
        mDialogDelegate.onCreate();

        setResult(0, null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_SCENE_ID, mSceneId);
        outState.putParcelable(SAVED_TAB_BAR_ITEM, mTabBarItem);
        outState.putString(SAVED_PRESENTATION_STYLE, mPresentationStyle.name());
        mPresentationDelegate.onSaveInstanceState(outState);
    }

    @Override
    @NonNull
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        inflateStyle();

        if (getShowsDialog()) {
            setStyle(STYLE_NORMAL, R.style.Theme_Nav_FullScreenDialog);
        }

        LayoutInflater layoutInflater = super.onGetLayoutInflater(savedInstanceState);
        if (getShowsDialog()) {
            return mDialogDelegate.onGetLayoutInflater(layoutInflater, savedInstanceState);
        }

        if (mStackDelegate.hasStackParent()) {
            return mStackDelegate.onGetLayoutInflater(layoutInflater, savedInstanceState);
        }

        return layoutInflater;
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

        if (getShowsDialog()) {
            mDialogDelegate.setupDialog();
        }

        if (mStackDelegate.hasStackParent()) {
            mStackDelegate.fitStackFragment();
        }

        if (isLeafAwesomeFragment()) {
            setBackgroundDrawable();
        }
    }

    @Override
    public void onDestroyView() {
        AppUtils.hideSoftInput(getWindow());
        super.onDestroyView();
    }

    public boolean isLeafAwesomeFragment() {
        return true;
    }

    private void setBackgroundDrawable() {
        if (getShowsDialog()) {
            setBackgroundForDialogWindow();
            return;
        }
        requireView().setBackground(new ColorDrawable(mStyle.getScreenBackgroundColor()));
    }

    private void setBackgroundForDialogWindow() {
        int color = mStyle.getScreenBackgroundColor();
        if (Color.alpha(color) < 255) {
            Window window = getWindow();
            window.setDimAmount(0);
            window.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    @NonNull
    public Window getWindow() {
        AwesomeFragment fragment = getDialogAwesomeFragment();
        if (fragment != null) {
            Dialog dialog = fragment.requireDialog();
            return dialog.getWindow();
        }

        return requireActivity().getWindow();
    }

    @Nullable
    public AwesomeFragment getDialogAwesomeFragment() {
        if (getShowsDialog()) {
            return this;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getDialogAwesomeFragment();
        }

        return null;
    }

    @NonNull
    public List<AwesomeFragment> getChildAwesomeFragments() {
        if (isAdded()) {
            return FragmentHelper.getFragments(getChildFragmentManager());
        }
        return Collections.emptyList();
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

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        //Log.i(TAG, getDebugTag() + "#onResume");
        if (childFragmentForAppearance() == null) {
            setNeedsStatusBarAppearanceUpdate();
            setNeedsNavigationBarAppearanceUpdate();
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
        if (isResumed() && isLeafAwesomeFragment()) {
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
        // Log.i(TAG, getDebugTag() + "  " + " transit:" + transit + " enter:" + enter + " nextAnim:" + nextAnim + " isAdd:" + isAdded() + " inRemoving:" + isRemoving());

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && FragmentHelper.isRemoving(parent)) {
            return AnimationUtils.loadAnimation(requireContext(), R.anim.nav_delay);
        }

        Animation anim = createOurAnimation(transit, enter, nextAnim);

        if (!mStackDelegate.drawTabBarIfNeeded(transit, enter, anim)) {
            mStackDelegate.drawScrimIfNeeded(transit, enter, anim);
        }

        return anim;
    }

    @Nullable
    private Animation createOurAnimation(int transit, boolean enter, int nextAnim) {
        TransitionAnimation animation = getAnimation();
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                return AnimationUtils.loadAnimation(requireContext(), nextAnim == 0 ? animation.enter : nextAnim);
            }
            return AnimationUtils.loadAnimation(requireContext(), nextAnim == 0 ? animation.exit : nextAnim);
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return AnimationUtils.loadAnimation(requireContext(), nextAnim == 0 ? animation.popEnter : nextAnim);
            }
            return AnimationUtils.loadAnimation(requireContext(), nextAnim == 0 ? animation.popExit : nextAnim);
        }

        return null;
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        mLifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

    public void setActivityRootFragment(AwesomeFragment root) {
        if (mPresentableActivity != null) {
            mPresentableActivity.setActivityRootFragment(root);
        }
    }

    public String getDebugTag() {
        if (getActivity() == null) {
            return null;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent == null) {
            return "#" + FragmentHelper.indexOf(this) + "-" + getClass().getSimpleName();
        }
        return parent.getDebugTag() + "#" + FragmentHelper.indexOf(this) + "-" + getClass().getSimpleName();
    }

    private String mSceneId;

    @NonNull
    public String getSceneId() {
        if (mSceneId == null) {
            mSceneId = UUID.randomUUID().toString();
        }
        return mSceneId;
    }

    protected final boolean dispatchBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (onPresentationContextBackPressed(fragmentManager)) {
            return true;
        }

        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment instanceof AwesomeFragment) {
            AwesomeFragment child = (AwesomeFragment) fragment;
            return child.dispatchBackPressed() || onBackPressed();
        }

        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            return (child != null && child.dispatchBackPressed()) || onBackPressed();
        }

        return onBackPressed();
    }

    private boolean onPresentationContextBackPressed(FragmentManager fragmentManager) {
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (!(fragment instanceof AwesomeFragment)) {
            return false;
        }

        if (!((AwesomeFragment) fragment).definesPresentationContext()) {
            return false;
        }

        int count = fragmentManager.getBackStackEntryCount();
        if (count == 0) {
            return false;
        }

        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
        AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        if (child == null) {
            return false;
        }

        boolean processed = child.dispatchBackPressed() || onBackPressed();
        if (!processed) {
            child.dismissFragment();
        }
        return true;
    }

    protected boolean onBackPressed() {
        if (getShowsDialog() && isCancelable()) {
            hideAsDialog();
            return true;
        }
        return false;
    }

    // -------- result --------

    private int mRequestCode;
    private int mResultCode;
    private Bundle mResult;

    public void setResult(int resultCode, Bundle data) {
        mResult = data;
        mResultCode = resultCode;
        if (definesPresentationContext()) {
            return;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
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
            dispatchChildFragmentResult(requestCode, resultCode, data, child);
            return;
        }

        if (this instanceof StackFragment) {
            AwesomeFragment child = ((StackFragment) this).getTopFragment();
            dispatchChildFragmentResult(requestCode, resultCode, data, child);
            return;
        }

        if (this instanceof DrawerFragment) {
            AwesomeFragment child = ((DrawerFragment) this).getContentFragment();
            dispatchChildFragmentResult(requestCode, resultCode, data, child);
            return;
        }

        List<AwesomeFragment> fragments = getChildAwesomeFragments();
        for (AwesomeFragment child : fragments) {
            child.onFragmentResult(requestCode, resultCode, data);
        }
    }

    private void dispatchChildFragmentResult(int requestCode, int resultCode, @Nullable Bundle data, @Nullable AwesomeFragment child) {
        if (child == null) {
            return;
        }
        child.onFragmentResult(requestCode, resultCode, data);
    }

    // ------- presentation -----

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode) {
        presentFragment(fragment, requestCode, () -> {
        });
    }

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode, @NonNull Runnable completion) {
        presentFragment(fragment, requestCode, completion, TransitionAnimation.Present);
    }

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> mPresentationDelegate.presentFragment(fragment, requestCode, completion, animation));
    }

    public void dismissFragment() {
        dismissFragment(() -> {
        });
    }

    public void dismissFragment(@NonNull Runnable completion) {
        dismissFragment(completion, TransitionAnimation.Present);
    }

    public void dismissFragment(@NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> mPresentationDelegate.dismissFragment(completion, animation));
    }

    @Nullable
    public AwesomeFragment getPresentedFragment() {
        return mPresentationDelegate.getPresentedFragment();
    }

    @Nullable
    public AwesomeFragment getPresentingFragment() {
        return mPresentationDelegate.getPresentingFragment();
    }

    public void setDefinesPresentationContext(boolean defines) {
        mPresentationDelegate.setDefinesPresentationContext(defines);
    }

    public boolean definesPresentationContext() {
        return mPresentationDelegate.definesPresentationContext();
    }

    private PresentationStyle mPresentationStyle = PresentationStyle.CurrentContext;

    public void setPresentationStyle(PresentationStyle style) {
        mPresentationStyle = style;
    }

    public PresentationStyle getPresentationStyle() {
        return mPresentationStyle;
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
        showAsDialog(dialog, requestCode, () -> {
        });
    }

    public void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> {
            mDialogDelegate.showAsDialog(dialog, requestCode, completion);
        });
    }

    /**
     * Dismiss the fragment as dialog.
     */
    public void hideAsDialog() {
        hideAsDialog(() -> {
        });
    }

    public void hideAsDialog(@NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> mDialogDelegate.hideAsDialog(completion));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mDialogDelegate.onDismiss();
    }

    // ------- statusBar --------

    @Nullable
    protected AwesomeFragment childFragmentForAppearance() {
        return null;
    }

    @NonNull
    protected BarStyle preferredStatusBarStyle() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredStatusBarStyle();
        }

        if (getShowsDialog()) {
            return BarStyle.LightContent;
        }
        return mStyle.getStatusBarStyle();
    }

    protected boolean preferredStatusBarHidden() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredStatusBarHidden();
        }

        if (getShowsDialog()) {
            return SystemUI.isStatusBarHidden(requireActivity().getWindow());
        }
        return mStyle.isStatusBarHidden();
    }

    protected int preferredStatusBarColor() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredStatusBarColor();
        }

        if (getShowsDialog()) {
            return Color.TRANSPARENT;
        }
        return mStyle.getStatusBarColor();
    }

    protected boolean preferredStatusBarColorAnimated() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredStatusBarColorAnimated();
        }
        if (getAnimation() == TransitionAnimation.None) {
            return false;
        }
        return mStyle.isStatusBarColorAnimated();
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

        setStatusBarHidden(preferredStatusBarHidden());
        setStatusBarStyle(preferredStatusBarStyle());
        setStatusBarColor(preferredStatusBarColor(), preferredStatusBarColorAnimated());
    }

    private void setStatusBarStyle(BarStyle barStyle) {
        SystemUI.setStatusBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    private void setStatusBarHidden(boolean hidden) {
        SystemUI.setStatusBarHidden(getWindow(), hidden);
    }

    private void setStatusBarColor(int color, boolean animated) {
        SystemUI.setStatusBarColor(getWindow(), color, animated);
    }

    public void appendStatusBarPadding(View view) {
        SystemUI.appendStatusBarPadding(requireContext(), view);
    }

    public void removeStatusBarPadding(View view) {
        SystemUI.removeStatusBarPadding(requireContext(), view);
    }

    // ------- NavigationBar --------
    @ColorInt
    protected int preferredNavigationBarColor() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredNavigationBarColor();
        }
        if (getShowsDialog()) {
            return mDialogDelegate.preferredNavigationBarColor();
        }
        if (mStyle.getNavigationBarColor() != Style.INVALID_COLOR) {
            return mStyle.getNavigationBarColor();
        }
        return mStyle.getScreenBackgroundColor();
    }

    @NonNull
    protected BarStyle preferredNavigationBarStyle() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredNavigationBarStyle();
        }
        if (AppUtils.isBlackColor(preferredNavigationBarColor(), 176)) {
            return BarStyle.LightContent;
        }
        return BarStyle.DarkContent;
    }

    protected boolean preferredNavigationBarHidden() {
        AwesomeFragment child = childFragmentForAppearance();
        if (child != null) {
            return child.preferredNavigationBarHidden();
        }

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

        setNavigationBarColor(preferredNavigationBarColor());
        setNavigationBarStyle(preferredNavigationBarStyle());
        setNavigationBarHidden(preferredNavigationBarHidden());
        setNavigationBarLayoutHidden(preferredNavigationBarHidden() ||
                Color.alpha(preferredNavigationBarColor()) < 255);
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
        if (this instanceof StackFragment) {
            return (StackFragment) this;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getStackFragment();
        }

        return null;
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
        return mStackDelegate.extendedLayoutIncludesToolbar();
    }

    public int getToolbarHeight() {
        return mStyle.getToolbarHeight();
    }

    public void setNeedsToolbarAppearanceUpdate() {
        mStackDelegate.setNeedsToolbarAppearanceUpdate();
    }

    public void setTitle(@StringRes int resId) {
        mStackDelegate.setTitle(requireContext(), resId);
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
