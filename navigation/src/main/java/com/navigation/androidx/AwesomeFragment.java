package com.navigation.androidx;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
    private static final String SAVED_STATE_DEFINES_PRESENTATION_CONTEXT = "defines_presentation_context";

    // ------- lifecycle methods -------
    private PresentableActivity presentableActivity;
    private final LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);
    protected Style mStyle;

    private DialogDelegate mDialogDelegate;
    private StackDelegate mStackDelegate;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof PresentableActivity)) {
            throw new IllegalArgumentException("Activity must implements PresentableActivity!");
        }
        presentableActivity = (PresentableActivity) activity;
        mDialogDelegate = new DialogDelegate(this);
        mStackDelegate = new StackDelegate(this);
        inflateStyle();
    }

    @Override
    public void onDetach() {
        presentableActivity = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            sceneId = savedInstanceState.getString(SAVED_SCENE_ID);
            tabBarItem = savedInstanceState.getParcelable(SAVED_TAB_BAR_ITEM);
            definesPresentationContext = savedInstanceState.getBoolean(SAVED_STATE_DEFINES_PRESENTATION_CONTEXT, false);
        }

        Bundle args = FragmentHelper.getArguments(this);
        boolean showAsDialog = args.getBoolean(ARGS_SHOW_AS_DIALOG, false);
        setShowsDialog(showAsDialog);
        setResult(0, null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_SCENE_ID, sceneId);
        outState.putParcelable(SAVED_TAB_BAR_ITEM, tabBarItem);
        outState.putBoolean(SAVED_STATE_DEFINES_PRESENTATION_CONTEXT, definesPresentationContext);
    }

    @Override
    @NonNull
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        inflateStyle();

        if (!getShowsDialog()) {
            if (mStackDelegate.hasStackParent()) {
                return mStackDelegate.onGetLayoutInflater(savedInstanceState);
            }

            return super.onGetLayoutInflater(savedInstanceState);
        }

        setStyle(STYLE_NORMAL, R.style.Theme_Nav_FullScreenDialog);
        super.onGetLayoutInflater(savedInstanceState);
        return mDialogDelegate.onGetLayoutInflater(savedInstanceState);
    }

    private void inflateStyle() {
        if (mStyle == null && presentableActivity != null && presentableActivity.getStyle() != null) {
            try {
                mStyle = presentableActivity.getStyle().clone();
                onCustomStyle(mStyle);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                mStyle = presentableActivity.getStyle();
            }
        }
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    @Override
    protected void performViewCreated() {
        super.performViewCreated();

        if (!getShowsDialog()) {
            View root = getView();
            if (root == null) {
                return;
            }
            if (!isParentFragment()) {
                setBackgroundDrawable(root, new ColorDrawable(mStyle.getScreenBackgroundColor()));
            }

            mStackDelegate.fitStackFragment(root);

        } else {
            setupDialog();
        }
    }

    @Override
    public void onDestroyView() {
        if (getView() != null) {
            AppUtils.hideSoftInput(getView());
        }
        super.onDestroyView();
    }

    private void setBackgroundDrawable(View root, Drawable drawable) {
        root.setBackground(drawable);
        if (!isInDialog()) {
            Window window = getWindow();
            if (window != null) {
                window.setBackgroundDrawable(AppUtils.copyDrawable(drawable));
            }
        }
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

    private TransitionAnimation animation = null;

    public void setAnimation(@Nullable TransitionAnimation animation) {
        this.animation = animation;
    }

    @NonNull
    public TransitionAnimation getAnimation() {
        if (this.animation == null) {
            this.animation = TransitionAnimation.None;
        }
        return this.animation;
    }

    @Override
    @CallSuper
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        // ---------
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

    // ------ lifecycle arch -------

    public void scheduleTaskAtStarted(Runnable runnable) {
        scheduleTaskAtStarted(runnable, false);
    }

    public void scheduleTaskAtStarted(Runnable runnable, boolean deferred) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable, deferred);
    }

    // ------- navigation ------

    private String sceneId;

    @NonNull
    public String getSceneId() {
        if (this.sceneId == null) {
            this.sceneId = UUID.randomUUID().toString();
        }
        return this.sceneId;
    }

    private boolean definesPresentationContext;

    public boolean definesPresentationContext() {
        return definesPresentationContext;
    }

    public void setDefinesPresentationContext(boolean defines) {
        definesPresentationContext = defines;
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
            if (!FragmentHelper.canPresentFragment(this, requireActivity())) {
                if (completion != null) {
                    completion.run();
                }
                onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
                return;
            }

            AwesomeFragment parent = getParentAwesomeFragment();
            if (parent != null) {
                if (definesPresentationContext()) {
                    presentFragmentSync(AwesomeFragment.this, fragment, requestCode, completion);
                } else {
                    parent.presentFragment(fragment, requestCode, completion);
                }
                return;
            }

            if (presentableActivity != null) {
                Bundle args = FragmentHelper.getArguments(fragment);
                args.putInt(ARGS_REQUEST_CODE, requestCode);
                presentableActivity.presentFragment(fragment, completion);
            }
        }, true);
    }

    private void presentFragmentSync(final AwesomeFragment target, final AwesomeFragment fragment, final int requestCode, @Nullable Runnable completion) {
        Bundle args = FragmentHelper.getArguments(fragment);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        fragment.setTargetFragment(target, requestCode);
        fragment.setDefinesPresentationContext(true);
        FragmentHelper.addFragmentToBackStack(target.getParentFragmentManager(), target.getContainerId(), fragment, TransitionAnimation.Present);
        if (completion != null) {
            completion.run();
        }
    }

    public void dismissFragment() {
        dismissFragment(null);
    }

    public void dismissFragment(@Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            if (isInDialog()) {
                throw new IllegalStateException("在 dialog 中， 不能执行此操作, 如需隐藏 dialog , 请调用 `hideDialog`");
            }

            AwesomeFragment parent = getParentAwesomeFragment();
            if (parent != null) {
                if (definesPresentationContext()) {
                    AwesomeFragment presented = getPresentedFragment();
                    if (presented != null) {
                        FragmentHelper.handleDismissFragment(this, presented, null);
                        if (completion != null) {
                            completion.run();
                        }
                        return;
                    }

                    AwesomeFragment target = (AwesomeFragment) getTargetFragment();
                    if (target != null) {
                        FragmentHelper.handleDismissFragment(target, this, this);
                    }

                    if (completion != null) {
                        completion.run();
                    }
                } else {
                    parent.dismissFragment(completion);
                }
                return;
            }

            if (presentableActivity != null) {
                presentableActivity.dismissFragment(this, completion);
            }
        }, true);
    }

    @Nullable
    public AwesomeFragment getPresentedFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (FragmentHelper.getIndexAtBackStack(this) == -1) {
                    if (FragmentHelper.getBackStackEntryCount(parent) != 0) {
                        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
                        AwesomeFragment presented = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
                        if (presented != null && presented.isAdded()) {
                            return presented;
                        }
                    }
                    return null;
                } else {
                    return FragmentHelper.getFragmentAfter(this);
                }
            } else {
                return parent.getPresentedFragment();
            }
        }

        if (presentableActivity != null) {
            return presentableActivity.getPresentedFragment(this);
        }

        return null;
    }

    @Nullable
    public AwesomeFragment getPresentingFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                AwesomeFragment target = (AwesomeFragment) getTargetFragment();
                if (target != null && target.isAdded()) {
                    return target;
                }
                return null;
            } else {
                return parent.getPresentingFragment();
            }
        }

        if (presentableActivity != null) {
            return presentableActivity.getPresentingFragment(this);
        }

        return null;
    }

    public void setActivityRootFragment(AwesomeFragment root) {
        if (presentableActivity != null) {
            presentableActivity.setActivityRootFragment(root);
        }
    }

    private int requestCode;
    private int resultCode;
    private Bundle result;

    public void setResult(int resultCode, Bundle data) {
        this.result = data;
        this.resultCode = resultCode;
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && !definesPresentationContext() && !getShowsDialog()) {
            parent.setResult(resultCode, data);
        }
    }

    public int getRequestCode() {
        if (requestCode == 0) {
            Bundle args = FragmentHelper.getArguments(this);
            requestCode = args.getInt(ARGS_REQUEST_CODE);
        }
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Bundle getResultData() {
        return result;
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

    @Nullable
    public AwesomeFragment getParentAwesomeFragment() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    public boolean isParentFragment() {
        return false;
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
        if (!getShowsDialog() && parent != null) {
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

    public void setStatusBarStyle(BarStyle barStyle) {
        SystemUI.setStatusBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    public void setStatusBarHidden(boolean hidden) {
        Window window = getWindow();
        if (window != null) {
            SystemUI.setStatusBarHidden(getWindow(), hidden);
        }
    }

    public void setStatusBarColor(int color, boolean animated) {
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
        if (!getShowsDialog() && parent != null) {
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

    public void setNavigationBarStyle(BarStyle barStyle) {
        SystemUI.setNavigationBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    public void setNavigationBarColor(int color) {
        SystemUI.setNavigationBarColor(getWindow(), color);
    }

    public void setNavigationBarHidden(boolean hidden) {
        SystemUI.setNavigationBarHidden(getWindow(), hidden);
    }

    private void setNavigationBarLayoutHidden(boolean hidden) {
        SystemUI.setNavigationBarLayoutHidden(getWindow(), hidden);
    }

    // ------ dialog -----

    /**
     * @deprecated call {@link #showDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    /**
     * @deprecated call {@link #showDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public int show(@NonNull FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag);
    }

    /**
     * @deprecated call {@link #hideDialog()} instead of this method.
     */
    @Deprecated
    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Nullable
    public Window getWindow() {
        if (isInDialog()) {
            AwesomeFragment fragment = this;
            while (fragment != null && !fragment.getShowsDialog()) {
                fragment = fragment.getParentAwesomeFragment();
            }

            if (fragment != null) {
                Dialog dialog = fragment.getDialog();
                if (dialog != null) {
                    return dialog.getWindow();
                }
            }
        }

        if (getActivity() != null) {
            return getActivity().getWindow();
        }

        return null;
    }

    public boolean isInDialog() {
        if (getShowsDialog()) {
            return true;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        return parent != null && parent.isInDialog();
    }

    protected void setupDialog() {
        mDialogDelegate.setupDialog();
    }

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        showDialog(dialog, requestCode, null);
    }

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            mDialogDelegate.showDialog(dialog, requestCode, completion);
        }, true);
    }

    /**
     * Dismiss the fragment as dialog.
     */
    public void hideDialog() {
        hideDialog(null);
    }

    public void hideDialog(@Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> mDialogDelegate.hideDialog(completion, false), true);
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
            throw new NullPointerException("NavigationFragment is null, make sure this fragment is wrapped in A NavigationFragment.");
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

        if (getShowsDialog()) {
            return null;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getTabBarFragment();
        }
        return null;
    }

    private TabBarItem tabBarItem;

    public void setTabBarItem(@Nullable TabBarItem item) {
        tabBarItem = item;
    }

    @Nullable
    public TabBarItem getTabBarItem() {
        return tabBarItem;
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

        if (getShowsDialog()) {
            return null;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getDrawerFragment();
        }
        return null;
    }

}
