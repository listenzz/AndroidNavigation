package com.navigation.androidx;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.InternalFragment;
import androidx.lifecycle.Lifecycle;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Listen on 2018/1/11.
 */

public abstract class AwesomeFragment extends InternalFragment {

    public static final String TAG = "Navigation";

    private static final String ARGS_REQUEST_CODE = "nav_request_code";
    static final String ARGS_SHOW_AS_DIALOG = "show_as_dialog";

    private static final String SAVED_TAB_BAR_ITEM = "nav_tab_bar_item";
    private static final String SAVED_ANIMATION_TYPE = "nav_animation_type";
    private static final String SAVED_SCENE_ID = "nav_scene_id";
    private static final String SAVED_STATE_DEFINES_PRESENTATION_CONTEXT = "defines_presentation_context";

    // ------- lifecycle methods -------
    private PresentableActivity presentableActivity;
    private final LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);
    protected Style style;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof PresentableActivity)) {
            throw new IllegalArgumentException("Activity must implements PresentableActivity!");
        }
        presentableActivity = (PresentableActivity) activity;
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
            String animationName = savedInstanceState.getString(SAVED_ANIMATION_TYPE);
            this.animationType = AnimationType.valueOf(animationName);
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
        outState.putString(SAVED_ANIMATION_TYPE, animationType.name());
        outState.putParcelable(SAVED_TAB_BAR_ITEM, tabBarItem);
        outState.putBoolean(SAVED_STATE_DEFINES_PRESENTATION_CONTEXT, definesPresentationContext);
    }

    @Override
    @NonNull
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        inflateStyle();

        if (!getShowsDialog()) {
            if (hasNavigationParent()) {
                LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
                layoutInflater = new NavigationLayoutInflater(requireContext(), layoutInflater);
                return layoutInflater;
            }

            return super.onGetLayoutInflater(savedInstanceState);
        }

        setStyle(STYLE_NORMAL, R.style.Theme_Nav_FullScreenDialog);

        super.onGetLayoutInflater(savedInstanceState);
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        Window window = getWindow();
        boolean isFloating = window != null && window.isFloating();
        if (getShowsDialog() && !isFloating) {
            layoutInflater = new DialogLayoutInflater(requireContext(), layoutInflater,
                    () -> {
                        if (isCancelable()) {
                            hideDialog(null);
                        }
                    });
        }

        return layoutInflater;
    }

    private void inflateStyle() {
        if (style == null && presentableActivity != null && presentableActivity.getStyle() != null) {
            try {
                style = presentableActivity.getStyle().clone();
                onCustomStyle(style);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                style = presentableActivity.getStyle();
            }
        }
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    private boolean callSuperOnViewCreated;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        if (!getShowsDialog()) {
            if (!isParentFragment()) {
                setBackgroundDrawable(root, new ColorDrawable(style.getScreenBackgroundColor()));
            }
            fitNavigationFragment(root);
        } else {
            setupDialog();
            animateIn();
        }

        callSuperOnViewCreated = true;
    }

    @Override
    public void onDestroyView() {
        if (getView() != null) {
            AppUtils.hideSoftInput(getView());
        }
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && !callSuperOnViewCreated) {
            throw new IllegalStateException("must invoke `super.onViewCreated` when override `onViewCreated`");
        }
    }

    private void setBackgroundDrawable(View root, Drawable drawable) {
        root.setBackground(drawable);
        if (!isInDialog()) {
            Window window = getWindow();
            if (window != null) {
                window.setBackgroundDrawable(null);
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
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        //Log.i(TAG, getDebugTag() + "#onPause");
    }

    @Override
    @CallSuper
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        // ---------
        // Log.d(TAG, getDebugTag() + "  " + animation.name() + " transit:" + transit + " enter:" + enter);

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && FragmentHelper.isRemoving(parent)) {
            return AnimationUtils.loadAnimation(context, R.anim.nav_delay);
        }

        TransitionAnimation animation = getAnimation();

        Animation anim = null;
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                anim = AnimationUtils.loadAnimation(context, animation.enter);
            } else {
                anim = AnimationUtils.loadAnimation(context, animation.exit);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                anim = AnimationUtils.loadAnimation(context, animation.popEnter);
            } else {
                anim = AnimationUtils.loadAnimation(context, animation.popExit);
            }
        }

        if (!drawTabBarIfNeeded(transit, enter, anim)) {
            drawScrimIfNeeded(transit, enter, anim);
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
        FragmentHelper.addFragmentToBackStack(target.requireFragmentManager(), target.getContainerId(), fragment, TransitionAnimation.Present);
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
                FragmentManager fragmentManager = requireFragmentManager();
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
        } else if (this instanceof NavigationFragment) {
            AwesomeFragment child = ((NavigationFragment) this).getTopFragment();
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

        return style.getStatusBarStyle();
    }

    protected boolean preferredStatusBarHidden() {
        if (getShowsDialog()) {
            return SystemUI.isStatusBarHidden(requireActivity().getWindow());
        }

        return style.isStatusBarHidden();
    }

    protected int preferredStatusBarColor() {
        if (getShowsDialog()) {
            return Color.TRANSPARENT;
        }

        return style.getStatusBarColor();
    }

    protected boolean preferredStatusBarColorAnimated() {
        return getAnimation() != TransitionAnimation.None && style.isStatusBarColorAnimated();
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
            if (getAnimationType() == AnimationType.Slide) {
                return requireActivity().getWindow().getNavigationBarColor();
            } else {
                return Color.TRANSPARENT;
            }
        }

        if (style.getNavigationBarColor() != Style.INVALID_COLOR) {
            return style.getNavigationBarColor();
        } else {
            return style.getScreenBackgroundColor();
        }
    }

    @NonNull
    protected BarStyle preferredNavigationBarStyle() {
        return AppUtils.isBlackColor(preferredNavigationBarColor(), 176) ? BarStyle.LightContent : BarStyle.DarkContent;
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
        setNavigationBarColor(fragment.preferredNavigationBarColor());
        setNavigationBarStyle(fragment.preferredNavigationBarStyle());
    }

    public void setNavigationBarStyle(BarStyle barStyle) {
        SystemUI.setNavigationBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    public void setNavigationBarColor(int color) {
        SystemUI.setNavigationBarColor(getWindow(), color);
    }

    // ------ dialog -----

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
        Window window = getWindow();
        SystemUI.setStatusBarTranslucent(window, true);

        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!dispatchBackPressed() && isCancelable()) {
                        hideDialog(null);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Dismiss the fragment as dialog.
     */
    public void hideDialog() {
        hideDialog(null);
    }

    public void hideDialog(@Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> hideDialogSync(completion, false), true);
    }

    private void hideDialogSync(@Nullable Runnable completion, boolean fromAnimation) {
        if (!isInDialog()) {
            throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
        }

        if (!getShowsDialog()) {
            AwesomeFragment parent = getParentAwesomeFragment();
            if (parent != null) {
                parent.hideDialog(completion);
            } else {
                throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
            }
            return;
        }

        AppUtils.hideSoftInput(getView());

        if (getAnimationType() != AnimationType.None && !fromAnimation) {
            if (animateOut(completion)) {
                return;
            }
        }

        if (isAdded() && !isDismissed()) {
            requireFragmentManager().beginTransaction().setMaxLifecycle(this, Lifecycle.State.STARTED).commit();
            dismiss();
        }

        if (completion != null) {
            completion.run();
        }
    }

    @Override
    protected void dismissInternal(boolean allowStateLoss, boolean fromOnDismiss) {
        super.dismissInternal(allowStateLoss, fromOnDismiss);
        Fragment target = getTargetFragment();
        if (target instanceof AwesomeFragment && target.isAdded() && fromOnDismiss) {
            FragmentManager fragmentManager = requireFragmentManager();
            fragmentManager.beginTransaction().setMaxLifecycle(target, Lifecycle.State.RESUMED).commit();
            FragmentHelper.executePendingTransactionsSafe(fragmentManager);
            AwesomeFragment fragment = (AwesomeFragment) target;
            fragment.onFragmentResult(getRequestCode(), getResultCode(), getResultData());
        }
    }

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

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        showDialog(dialog, requestCode, null);
    }

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> {
            if (!FragmentHelper.canShowDialog(this, requireActivity())) {
                if (completion != null) {
                    completion.run();
                }
                onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
                return;
            }
            showDialogSync(AwesomeFragment.this, dialog, requestCode, completion);
        }, true);
    }

    private void showDialogSync(AwesomeFragment target, AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        Bundle args = FragmentHelper.getArguments(dialog);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        args.putBoolean(ARGS_SHOW_AS_DIALOG, true);
        dialog.setTargetFragment(target, requestCode);
        FragmentManager fragmentManager = target.requireFragmentManager();
        fragmentManager.beginTransaction().setMaxLifecycle(target, Lifecycle.State.STARTED).commit();
        dialog.show(fragmentManager, dialog.getSceneId());
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
        if (completion != null) {
            completion.run();
        }
    }

    private AnimationType animationType = AnimationType.None;

    public void setAnimationType(@NonNull AnimationType type) {
        this.animationType = type;
    }

    @NonNull
    public AnimationType getAnimationType() {
        return this.animationType;
    }

    private void animateIn() {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return;
        }

        AnimationType type = getAnimationType();
        boolean shouldAnimated = type != AnimationType.None;

        if (!shouldAnimated) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
            if (layoutParams.gravity == Gravity.BOTTOM) {
                shouldAnimated = true;
                type = AnimationType.Slide;
                setAnimationType(type);
            }
        }

        if (shouldAnimated) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            if (type == AnimationType.Slide) {
                animateUpIn(contentView);
            }
        }
    }

    private boolean animateOut(@Nullable Runnable completion) {
        View root = getView();
        AnimationType type = getAnimationType();
        boolean shouldAnimated = type != AnimationType.None && root instanceof DialogFrameLayout;
        if (shouldAnimated) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            if (type == AnimationType.Slide) {
                animateDownOut(contentView, completion);
                return true;
            }
        }
        return false;
    }

    private void animateDownOut(@NonNull final View contentView, @Nullable Runnable completion) {
        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f
        );
        AlphaAnimation alpha = new AlphaAnimation(1, 0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(translate);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(200);
        set.setFillAfter(true);
        set.setAnimationListener(createAnimationListener(contentView, completion));
        contentView.startAnimation(set);
    }

    private void animateUpIn(@NonNull final View contentView) {
        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f
        );
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(translate);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(200);
        set.setFillAfter(true);
        contentView.startAnimation(set);
    }

    private Animation.AnimationListener createAnimationListener(@NonNull final View animationView, @Nullable Runnable completion) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationView.post(() -> AwesomeFragment.this.hideDialogSync(completion, true));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    // ------ NavigationFragment -----
    @NonNull
    public NavigationFragment requireNavigationFragment() {
        NavigationFragment navigationFragment = getNavigationFragment();
        if (navigationFragment == null) {
            throw new NullPointerException("NavigationFragment is null, make sure this fragment is wrapped in A NavigationFragment.");
        }
        return navigationFragment;
    }

    @Nullable
    public NavigationFragment getNavigationFragment() {
        if (this instanceof NavigationFragment) {
            return (NavigationFragment) this;
        }

        if (getShowsDialog()) {
            return null;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getNavigationFragment();
        }
        return null;
    }

    public boolean isNavigationRoot() {
        NavigationFragment navigationFragment = getNavigationFragment();
        if (navigationFragment != null) {
            AwesomeFragment fragment = navigationFragment.getRootFragment();
            return fragment == this;
        }
        return false;
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

    boolean shouldHideTabBarWhenPushed() {
        NavigationFragment navigationFragment = getNavigationFragment();
        AwesomeFragment root;
        if (navigationFragment != null) {
            root = navigationFragment.getRootFragment();
            if (root != null && root.isAdded()) {
                return root.hideTabBarWhenPushed();
            }
        }
        return true;
    }

    private boolean drawTabBarIfNeeded(int transit, boolean enter, Animation anim) {
        Fragment parent = getParentFragment();
        if (!(parent instanceof NavigationFragment)) {
            return false;
        }

        NavigationFragment navigationFragment = (NavigationFragment) parent;
        TabBarFragment tabBarFragment = navigationFragment.getTabBarFragment();

        if (tabBarFragment == null) {
            return false;
        }

        if (navigationFragment != tabBarFragment.getSelectedFragment()) {
            return false;
        }

        if (!shouldHideTabBarWhenPushed()) {
            return false;
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN && !enter) {
            if (this == navigationFragment.getRootFragment()) {
                drawTabBar(tabBarFragment, anim.getDuration(), true);
                tabBarFragment.hideTabBarAnimated(anim);
                return true;
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
            if (this == navigationFragment.getRootFragment()) {
                drawTabBar(tabBarFragment, anim.getDuration(), false);
                tabBarFragment.showTabBarAnimated(anim);
                return true;
            }
        }

        return false;
    }

    private void drawScrimIfNeeded(int transit, boolean enter, Animation anim) {
        Fragment parent = getParentFragment();
        if (!(parent instanceof NavigationFragment)) {
            return;
        }

        NavigationFragment navigationFragment = (NavigationFragment) parent;
        TransitionAnimation animation = getAnimation();
        if (animation.exit == animation.popEnter) {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && !enter) {
                if (getView() != null) {
                    ViewCompat.setTranslationZ(getView(), -1f);
                }
                drawScrim(navigationFragment, anim.getDuration(), true);
            }
        } else {
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN && !enter) {
                drawScrim(navigationFragment, anim.getDuration(), true);
            } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE && enter) {
                drawScrim(navigationFragment, anim.getDuration(), false);
            }
        }
    }

    private void drawScrim(@NonNull NavigationFragment navigationFragment, long duration, boolean open) {
        if (navigationFragment.getView() == null) {
            return;
        }

        int vWidth = navigationFragment.getView().getWidth();
        int vHeight = navigationFragment.getView().getHeight();

        ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
        colorDrawable.setBounds(0, 0, vWidth, vHeight);

        View root = getView();
        if (root instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) root;
            frameLayout.setForeground(colorDrawable);
            int scrimAlpha = style.getScrimAlpha();
            ValueAnimator valueAnimator = open ? ValueAnimator.ofInt(0, scrimAlpha) : ValueAnimator.ofInt(scrimAlpha, 0);
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(animation -> {
                int value = (Integer) animation.getAnimatedValue();
                colorDrawable.setColor(value << 24);
            });
            valueAnimator.start();

            root.postDelayed(() -> {
                if (isAdded()) {
                    frameLayout.setForeground(null);
                }
            }, duration);
        }

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
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        bitmapDrawable.setBounds(0, vHeight - tabBar.getHeight(),
                tabBar.getMeasuredWidth(), vHeight);
        bitmapDrawable.setGravity(Gravity.BOTTOM);

        ColorDrawable colorDrawable = new ColorDrawable(0x00000000);
        colorDrawable.setBounds(0, 0, vWidth, vHeight);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{bitmapDrawable, colorDrawable,});
        layerDrawable.setBounds(0, 0, vWidth, vHeight);

        View root = getView();
        if (root instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) root;
            frameLayout.setForeground(layerDrawable);
            int scrimAlpha = style.getScrimAlpha();
            ValueAnimator valueAnimator = open ? ValueAnimator.ofInt(0, scrimAlpha) : ValueAnimator.ofInt(scrimAlpha, 0);
            valueAnimator.setDuration(duration);
            valueAnimator.addUpdateListener(animation -> {
                int value = (Integer) animation.getAnimatedValue();
                colorDrawable.setColor(value << 24);
            });
            valueAnimator.start();

            if (isAdded()) {
                root.postDelayed(() -> {
                    if (isAdded()) {
                        frameLayout.setForeground(null);
                    }
                }, duration + 16);
            }
        }
    }

    private volatile AwesomeToolbar toolbar;

    public AwesomeToolbar getAwesomeToolbar() {
        return toolbar;
    }

    private void fitNavigationFragment(@NonNull View root) {
        if (hasNavigationParent()) {
            createAwesomeToolbar(root);
            adjustBottomPaddingForTabBar(root);
        }
    }

    private boolean hasNavigationParent() {
        AwesomeFragment parent = getParentAwesomeFragment();
        return (parent instanceof NavigationFragment);
    }

    private void createAwesomeToolbar(@NonNull View root) {
        AwesomeToolbar toolbar = onCreateAwesomeToolbar(root);
        if (toolbar != null) {
            customAwesomeToolbar(toolbar);
        }
        this.toolbar = toolbar;
    }

    private void adjustBottomPaddingForTabBar(final View root) {
        int index = FragmentHelper.getIndexAtBackStack(this);
        if (index == 0 || !shouldHideTabBarWhenPushed()) {
            int color = Color.parseColor(style.getTabBarBackgroundColor());
            if (Color.alpha(color) == 255) {
                TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                    int bottomPadding = (int) getResources().getDimension(R.dimen.nav_tab_bar_height);
                    root.setPadding(0, 0, 0, bottomPadding);
                }
            }
        }
    }

    @Nullable
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        if (getView() == null || getContext() == null) return null;
        int toolbarHeight = getToolbarHeight();
        AwesomeToolbar toolbar = new AwesomeToolbar(getContext());
        FrameLayout frameLayout = (FrameLayout) parent;
        frameLayout.addView(toolbar, new FrameLayout.LayoutParams(-1, toolbarHeight));
        appendStatusBarPadding(toolbar);
        appendToolbarPadding(parent);
        return toolbar;
    }

    private void appendToolbarPadding(View parent) {
        if (!extendedLayoutIncludesToolbar()) {
            FrameLayout frameLayout = (FrameLayout) parent;
            if (frameLayout.getChildCount() > 1) {
                View child = frameLayout.getChildAt(0);
                int statusBarHeight = SystemUI.getStatusBarHeight(requireContext());
                int toolbarHeight = getToolbarHeight();
                child.setPadding(0, statusBarHeight + toolbarHeight, 0, 0);
            }
        }
    }

    protected boolean extendedLayoutIncludesToolbar() {
        int color = preferredToolbarColor();
        float alpha = preferredToolbarAlpha();
        return Color.alpha(color) < 255 || alpha < 1.0;
    }

    public int getToolbarHeight() {
        return style.getToolbarHeight();
    }

    private void customAwesomeToolbar(AwesomeToolbar toolbar) {
        toolbar.setBackgroundColor(preferredToolbarColor());
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
        toolbar.setAlpha(preferredToolbarAlpha());

        if (!isNavigationRoot()) {
            if (shouldHideBackButton()) {
                toolbar.setNavigationIcon(null);
                toolbar.setNavigationOnClickListener(null);
            } else if (leftBarButtonItem == null && leftBarButtonItems == null) {
                toolbar.setNavigationIcon(style.getBackIcon());
                toolbar.setNavigationOnClickListener(view -> {
                    NavigationFragment navigationFragment = getNavigationFragment();
                    if (navigationFragment != null) {
                        navigationFragment.dispatchBackPressed();
                    }
                });
            }
        }
    }

    protected int preferredToolbarColor() {
        return style.getToolbarBackgroundColor();
    }

    @FloatRange(from = 0f, to = 1.0f)
    protected float preferredToolbarAlpha() {
        return style.getToolbarAlpha();
    }

    public void setNeedsToolbarAppearanceUpdate() {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            customAwesomeToolbar(toolbar);

            if (leftBarButtonItems != null) {
                for (ToolbarButtonItem item : leftBarButtonItems) {
                    item.setTintColor(style.getToolbarTintColor(), style.getToolbarBackgroundColor());
                }
            } else if (leftBarButtonItem != null) {
                leftBarButtonItem.setTintColor(style.getToolbarTintColor(), style.getToolbarBackgroundColor());
            }

            if (rightBarButtonItems != null) {
                for (ToolbarButtonItem item : rightBarButtonItems) {
                    item.setTintColor(style.getToolbarTintColor(), style.getToolbarBackgroundColor());
                }
            } else if (rightBarButtonItem != null) {
                rightBarButtonItem.setTintColor(style.getToolbarTintColor(), style.getToolbarBackgroundColor());
            }
        }
    }

    public void setTitle(@StringRes int resId) {
        if (getContext() != null) {
            setTitle(getContext().getText(resId));
        }
    }

    public void setTitle(CharSequence title) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.setAwesomeTitle(title);
        }
    }

    private ToolbarButtonItem[] leftBarButtonItems;

    public void setLeftBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        leftBarButtonItems = barButtonItems;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.clearLeftButtons();
            if (barButtonItems == null) {
                if (!isNavigationRoot() && !shouldHideBackButton()) {
                    toolbar.setNavigationIcon(style.getBackIcon());
                    toolbar.setNavigationOnClickListener(view -> {
                        NavigationFragment navigationFragment = getNavigationFragment();
                        if (navigationFragment != null) {
                            navigationFragment.dispatchBackPressed();
                        }
                    });
                }
                return;
            }

            for (ToolbarButtonItem barButtonItem : barButtonItems) {
                toolbar.addLeftButton(barButtonItem);
            }
        }
    }

    @Nullable
    public ToolbarButtonItem[] getLeftBarButtonItems() {
        return leftBarButtonItems;
    }

    private ToolbarButtonItem[] rightBarButtonItems;

    public void setRightBarButtonItems(@Nullable ToolbarButtonItem[] barButtonItems) {
        rightBarButtonItems = barButtonItems;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.clearRightButtons();
            if (barButtonItems == null) {
                return;
            }
            for (ToolbarButtonItem barButtonItem : barButtonItems) {
                toolbar.addRightButton(barButtonItem);
            }
        }
    }

    @Nullable
    public ToolbarButtonItem[] getRightBarButtonItems() {
        return rightBarButtonItems;
    }

    private ToolbarButtonItem leftBarButtonItem;

    public void setLeftBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        leftBarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                toolbar.clearLeftButton();
                if (!isNavigationRoot() && !shouldHideBackButton()) {
                    toolbar.setNavigationIcon(style.getBackIcon());
                    toolbar.setNavigationOnClickListener(view -> {
                        NavigationFragment navigationFragment = getNavigationFragment();
                        if (navigationFragment != null) {
                            navigationFragment.dispatchBackPressed();
                        }
                    });
                }
                return;
            }
            toolbar.setLeftButton(barButtonItem);
            barButtonItem.attach(toolbar.getLeftButton());
        }
    }

    @Nullable
    public ToolbarButtonItem getLeftBarButtonItem() {
        return leftBarButtonItem;
    }

    private ToolbarButtonItem rightBarButtonItem;

    public void setRightBarButtonItem(@Nullable ToolbarButtonItem barButtonItem) {
        rightBarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                toolbar.clearRightButton();
                return;
            }
            toolbar.setRightButton(barButtonItem);
            barButtonItem.attach(toolbar.getRightButton());
        }
    }

    @Nullable
    public ToolbarButtonItem getRightBarButtonItem() {
        return rightBarButtonItem;
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
