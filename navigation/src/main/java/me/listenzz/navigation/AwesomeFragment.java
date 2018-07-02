package me.listenzz.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
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
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Listen on 2018/1/11.
 */

public abstract class AwesomeFragment extends DialogFragment {

    public static final String TAG = "Navigation";

    private static final String ARGS_SCENE_ID = "nav_scene_id";
    private static final String ARGS_REQUEST_CODE = "nav_request_code";
    private static final String ARGS_ANIMATION = "nav_animation";
    private static final String ARGS_ANIMATION_TYPE = "nav_animation_type";
    private static final String ARGS_TAB_BAR_ITEM = "nav_tab_bar_item";
    private static final String SAVED_STATE_BOTTOM_PADDING_KEY = "bottom_padding";

    // ------- lifecycle methods -------
    private PresentableActivity presentableActivity;
    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);
    protected Style style;
    private int bottomPadding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof PresentableActivity)) {
            throw new IllegalArgumentException("Activity must implements PresentableActivity!");
        }
        presentableActivity = (PresentableActivity) activity;
        // Log.i(TAG, getDebugTag() + "#onAttach");
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
            bottomPadding = savedInstanceState.getInt(SAVED_STATE_BOTTOM_PADDING_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE_BOTTOM_PADDING_KEY, bottomPadding);
    }

    @Override
    @NonNull
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        if (getShowsDialog()) {
            setStyle(0, R.style.Theme_Nav_FullScreenDialog);
        }

        super.onGetLayoutInflater(savedInstanceState);
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        if (getShowsDialog() && !getWindow().isFloating()) {
            layoutInflater = new DialogLayoutInflater(requireContext(), layoutInflater,
                    new DialogFrameLayout.OnTouchOutsideListener() {
                        @Override
                        public void onTouchOutside() {
                            if (isCancelable()) {
                                dismissDialog();
                            }
                        }
                    });
        }

        if (style == null) {
            try {
                style = presentableActivity.getStyle().clone();
                onCustomStyle(style);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                style = presentableActivity.getStyle();
            }
        }
        return layoutInflater;
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    boolean callSuperOnViewCreated;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        if (!getShowsDialog()) {
            if (!isParentFragment()) {
                setBackgroundDrawable(root, new ColorDrawable(style.getScreenBackgroundColor()));
            }
            if (getParentAwesomeFragment() == null) {
                fixKeyboardBugAtKitkat(root, isStatusBarTranslucent());
            }
            handleNavigationFragmentStuff(root);
        } else {
            setupDialog();
            scheduleTaskAtStarted(new Runnable() {
                @Override
                public void run() {
                    animateIn();
                }
            });
        }
        callSuperOnViewCreated = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && !callSuperOnViewCreated) {
            throw new IllegalStateException("you should call super when override `onViewCreated`");
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
        if (!getShowsDialog()) {
            root.setBackground(drawable);
            getWindow().setBackgroundDrawable(null);
        }
    }

    private boolean viewAppear;

    private void notifyViewAppear(boolean appear) {
        if (viewAppear != appear) {
            viewAppear = appear;
            if (appear) {
                onViewAppear();
            } else {
                onViewDisappear();
            }
        }
    }

    @CallSuper
    protected void onViewAppear() {
        if (childFragmentForAppearance() == null) {
            setNeedsStatusBarAppearanceUpdate();
        }
    }

    @CallSuper
    protected void onViewDisappear() {

    }

    @Override
    @CallSuper
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint() && !isFragmentHidden()) {
            notifyViewAppear(true);
        }
    }

    @Override
    @CallSuper
    public void onPause() {
        super.onPause();
        notifyViewAppear(false);
    }

    @Override
    @CallSuper
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!isFragmentHidden()) {
            if (isResumed() && getUserVisibleHint()) {
                notifyViewAppear(true);
            }
        } else {
            notifyViewAppear(false);
        }

        List<AwesomeFragment> fragments = getChildFragmentsAtAddedList();
        for (AwesomeFragment fragment : fragments) {
            fragment.onHiddenChanged(hidden);
        }
    }

    public boolean isFragmentHidden() {
        boolean hidden = super.isHidden();
        if (hidden) {
            return true;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        return parent != null && parent.isFragmentHidden();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isResumed() && !isFragmentHidden()) {
                notifyViewAppear(true);
            }
        } else {
            notifyViewAppear(false);
        }

        List<AwesomeFragment> fragments = getChildFragmentsAtAddedList();
        for (AwesomeFragment fragment : fragments) {
            fragment.setUserVisibleHint(isVisibleToUser);
        }
    }

    @Override
    public boolean getUserVisibleHint() {
        boolean isVisibleToUser = super.getUserVisibleHint();
        if (!isVisibleToUser) {
            return false;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        return parent == null || parent.getUserVisibleHint();
    }

    @Override
    @CallSuper
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        PresentAnimation animation = getAnimation();

        handleHideBottomBarWhenPushed(transit, enter, animation);
        // ---------
        // Log.d(TAG, getDebugTag() + "  " + animation.name() + " transit:" + transit + " enter:" + enter + " nextAnim:" + nextAnim);

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && parent.isRemoving()) {
            return AnimationUtils.loadAnimation(getContext(), R.anim.nav_delay);
        }

        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                return AnimationUtils.loadAnimation(getContext(), animation.enter);
            } else {
                return AnimationUtils.loadAnimation(getContext(), animation.exit);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return AnimationUtils.loadAnimation(getContext(), animation.popEnter);
            } else {
                return AnimationUtils.loadAnimation(getContext(), animation.popExit);
            }
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    // ------ lifecycle arch -------

    public void scheduleTaskAtStarted(Runnable runnable) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

    // ------- navigation ------

    private String sceneId;

    @NonNull
    public String getSceneId() {
        if (this.sceneId == null) {
            Bundle args = FragmentHelper.getArguments(this);
            String sceneId = args.getString(ARGS_SCENE_ID);
            if (sceneId == null) {
                sceneId = UUID.randomUUID().toString();
                args.putString(ARGS_SCENE_ID, sceneId);
            }

            this.sceneId = sceneId;
        }
        return this.sceneId;
    }

    public void presentFragment(final AwesomeFragment fragment, int requestCode) {
        final AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.presentFragment(fragment, requestCode);
        } else if (presentableActivity != null) {
            Bundle args = FragmentHelper.getArguments(fragment);
            args.putInt(ARGS_REQUEST_CODE, requestCode);
            presentableActivity.presentFragment(fragment);
        }
    }

    public void dismissFragment() {
        if (getShowsDialog()) {
            throw new IllegalStateException("似乎该 fragment 是以 dialog 的形式呈现，使用 `dismissDialog` 来关闭更合适");
        }
        final AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.setResult(resultCode, result);
            parent.dismissFragment();
            return;
        }

        if (presentableActivity != null) {
            presentableActivity.dismissFragment(this);
        }
    }

    public AwesomeFragment getPresentedFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getPresentedFragment();
        }
        return presentableActivity.getPresentedFragment(this);
    }

    public AwesomeFragment getPresentingFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            return parent.getPresentingFragment();
        }
        return presentableActivity.getPresentingFragment(this);
    }

    public void setActivityRootFragment(AwesomeFragment root) {
        presentableActivity.setActivityRootFragment(root);
    }

    private int requestCode;
    private int resultCode;
    private Bundle result;

    public void setResult(int resultCode, Bundle data) {
        this.result = data;
        this.resultCode = resultCode;
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
            child.onFragmentResult(requestCode, resultCode, data);
        } else if (this instanceof NavigationFragment) {
            AwesomeFragment child = ((NavigationFragment) this).getTopFragment();
            child.onFragmentResult(requestCode, resultCode, data);
        } else if (this instanceof DrawerFragment) {
            AwesomeFragment child = ((DrawerFragment) this).getContentFragment();
            child.onFragmentResult(requestCode, resultCode, data);
        } else {
            List<AwesomeFragment> fragments = getChildFragmentsAtAddedList();
            for (AwesomeFragment child : fragments) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        }
    }

    protected boolean dispatchBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null) {
            AwesomeFragment child = (AwesomeFragment) fragment;
            return child.dispatchBackPressed() || onBackPressed();
        } else if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            return child.dispatchBackPressed() || onBackPressed();
        } else {
            return onBackPressed();
        }
    }

    protected boolean onBackPressed() {
        return false;
    }

    public String getDebugTag() {
        if (getActivity() == null) {
            return null;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent == null) {
            return "#" + getIndexAtAddedList() + "-" + getClass().getSimpleName();
        } else {
            return parent.getDebugTag() + "#" + getIndexAtAddedList() + "-" + getClass().getSimpleName();
        }
    }

    public int getChildFragmentCountAtBackStack() {
        FragmentManager fragmentManager = getChildFragmentManager();
        return fragmentManager.getBackStackEntryCount();
    }

    public int getIndexAtBackStack() {
        return FragmentHelper.findIndexAtBackStack(requireFragmentManager(), this);
    }

    public int getIndexAtAddedList() {
        List<Fragment> fragments = requireFragmentManager().getFragments();
        return fragments.indexOf(this);
    }

    public List<AwesomeFragment> getChildFragmentsAtAddedList() {
        List<AwesomeFragment> children = new ArrayList<>();
        if (isAdded()) {
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
                Fragment fragment = fragments.get(i);
                if (fragment instanceof AwesomeFragment) {
                    children.add((AwesomeFragment) fragment);
                }
            }
        }
        return children;
    }

    public AwesomeFragment getParentAwesomeFragment() {
        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    private PresentAnimation animation = null;

    public void setAnimation(PresentAnimation animation) {
        Bundle bundle = FragmentHelper.getArguments(this);
        bundle.putString(ARGS_ANIMATION, animation.name());
        this.animation = animation;
    }

    public PresentAnimation getAnimation() {
        if (animation == null) {
            Bundle bundle = FragmentHelper.getArguments(this);
            String animationName = bundle.getString(ARGS_ANIMATION);
            if (animationName != null) {
                animation = PresentAnimation.valueOf(animationName);
            } else {
                animation = PresentAnimation.None;
            }
        }
        return animation;
    }

    public boolean isParentFragment() {
        return false;
    }

    // ------- statusBar --------

    protected BarStyle preferredStatusBarStyle() {
        // Log.w(TAG, getDebugTag() + " #preferredStatusBarStyle");
        AwesomeFragment childFragmentForStatusBarStyle = childFragmentForAppearance();
        if (childFragmentForStatusBarStyle != null) {
            return childFragmentForStatusBarStyle.preferredStatusBarStyle();
        }
        return style.getStatusBarStyle();
    }

    protected boolean preferredStatusBarHidden() {
        AwesomeFragment childFragmentForStatusBarHidden = childFragmentForAppearance();
        if (childFragmentForStatusBarHidden != null) {
            return childFragmentForStatusBarHidden.preferredStatusBarHidden();
        }
        return style.isStatusBarHidden();
    }

    protected int preferredStatusBarColor() {
        AwesomeFragment childFragmentForStatusBarColor = childFragmentForAppearance();
        if (childFragmentForStatusBarColor != null) {
            return childFragmentForStatusBarColor.preferredStatusBarColor();
        }
        return getShowsDialog() ? Color.TRANSPARENT : style.getStatusBarColor();
    }

    protected boolean preferredStatusBarColorAnimated() {
        AwesomeFragment childFragmentForStatusBarColor = childFragmentForAppearance();
        if (childFragmentForStatusBarColor != null) {
            return childFragmentForStatusBarColor.preferredStatusBarColorAnimated();
        }
        return getAnimation() != PresentAnimation.None && style.isStatusBarColorAnimated();
    }

    protected AwesomeFragment childFragmentForAppearance() {
        return null;
    }

    public void setNeedsStatusBarAppearanceUpdate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        // Log.i(TAG, getDebugTag() + "#setNeedsStatusBarAppearanceUpdate");

        if (getShowsDialog()) {
            Activity activity = requireActivity();
            int activityWindowFlags = activity.getWindow().getAttributes().flags;
            boolean hidden = (activityWindowFlags
                    & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0 || preferredStatusBarHidden();
            setStatusBarHidden(hidden);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isDark = preferredStatusBarStyle() == BarStyle.DarkContent || (activity.getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;
                setStatusBarStyle(isDark ? BarStyle.DarkContent : BarStyle.LightContent);
            }

            int color = preferredStatusBarColor();
            if (color != Color.TRANSPARENT) {
                boolean shouldAdjustForWhiteStatusBar = !AppUtils.isBlackColor(preferredStatusBarColor(), 176);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !AppUtils.isMiuiV6()) {
                    shouldAdjustForWhiteStatusBar = shouldAdjustForWhiteStatusBar && preferredStatusBarStyle() == BarStyle.LightContent;
                }
                if (shouldAdjustForWhiteStatusBar) {
                    color = Color.parseColor("#B0B0B0");
                }
            }
            setStatusBarColor(color, false);
            return;
        }

        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.setNeedsStatusBarAppearanceUpdate();
        } else {

            // statusBarHidden
            setStatusBarHidden(preferredStatusBarHidden());

            // statusBarStyle
            setStatusBarStyle(preferredStatusBarStyle());

            // statusBarColor
            int color = preferredStatusBarColor();
            boolean shouldAdjustForWhiteStatusBar = !AppUtils.isBlackColor(preferredStatusBarColor(), 176);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !AppUtils.isMiuiV6()) {
                shouldAdjustForWhiteStatusBar = shouldAdjustForWhiteStatusBar && preferredStatusBarStyle() == BarStyle.LightContent;
            }
            if (shouldAdjustForWhiteStatusBar) {
                color = Color.parseColor("#B0B0B0");
            }
            if (isStatusBarTranslucent() && color == preferredToolbarColor()) {
                color = Color.TRANSPARENT;
            }
            boolean animated = preferredStatusBarColorAnimated();
            setStatusBarColor(color, animated);
        }
    }

    public void setStatusBarStyle(BarStyle barStyle) {
        AppUtils.setStatusBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
    }

    public void setStatusBarHidden(boolean hidden) {
        AppUtils.setStatusBarHidden(getWindow(), hidden);
    }

    public void setStatusBarColor(int color, boolean animated) {
        AppUtils.setStatusBarColor(getWindow(), color, animated);
    }

    public void setStatusBarTranslucent(boolean translucent) {
        if (getShowsDialog()) {
            AppUtils.setStatusBarTranslucent(getWindow(), translucent);
        } else {
            presentableActivity.setStatusBarTranslucent(translucent);
        }
    }

    public boolean isStatusBarTranslucent() {
        return presentableActivity.isStatusBarTranslucent();
    }

    @CallSuper
    protected void onStatusBarTranslucentChanged(boolean translucent) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (translucent) {
                appendStatusBarPadding(toolbar, getToolbarHeight());
            } else {
                removeStatusBarPadding(toolbar, getToolbarHeight());
            }
        }

        if (getView() != null) {
            fixKeyboardBugAtKitkat(getView(), translucent);
        }

        List<AwesomeFragment> children = getChildFragmentsAtAddedList();
        for (int i = 0, size = children.size(); i < size; i++) {
            AwesomeFragment child = children.get(i);
            child.onStatusBarTranslucentChanged(translucent);
        }
    }

    public void appendStatusBarPadding(View view, int viewHeight) {
        AppUtils.appendStatusBarPadding(view, viewHeight);
    }

    public void removeStatusBarPadding(View view, int viewHeight) {
        AppUtils.removeStatusBarPadding(view, viewHeight);
    }

    private SoftInputLayoutListener globalLayoutListener;

    private void fixKeyboardBugAtKitkat(View root, boolean isStatusBarTranslucent) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            if (isStatusBarTranslucent) {
                if (globalLayoutListener == null) {
                    globalLayoutListener = new SoftInputLayoutListener(root);
                    root.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
                }
            } else {
                if (globalLayoutListener != null) {
                    root.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                    root.getLayoutParams().height = globalLayoutListener.getInitialHeight();
                    root.requestLayout();
                    globalLayoutListener = null;
                }
            }
        }
    }

    // ------ dialog -----

    public Window getWindow() {
        if (getDialog() != null) {
            return getDialog().getWindow();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarTranslucent(true);
        } else {
            setStatusBarTranslucent(presentableActivity.isStatusBarTranslucent());
        }

        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!dispatchBackPressed() && isCancelable()) {
                        dismissDialog();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * @deprecated call {@link #dismissDialog()} instead of this method.
     */
    @Deprecated
    @Override
    public void dismiss() {
        if (!isInDialog()) {
            throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
        } else {
            if (getShowsDialog()) {
                super.dismiss();
            } else {
                AwesomeFragment parent = getParentAwesomeFragment();
                parent.setResult(getResultCode(), getResultData());
                parent.dismissDialog();
            }
        }
    }

    private boolean animatingOut = false;

    /**
     * Dismiss the fragment as dialog.
     */
    public void dismissDialog() {
        if (animatingOut) {
            return;
        }

        if (getAnimationType() != AnimationType.None) {
            animateOut();
        } else {
            AppUtils.hideSoftInput(getView());
            dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Fragment target = getTargetFragment();
        if (target != null && target instanceof AwesomeFragment && target.isAdded() && !target.isRemoving()) {
            AwesomeFragment fragment = (AwesomeFragment) target;
            fragment.onFragmentResult(getTargetRequestCode(), getResultCode(), getResultData());
        }
        super.onDismiss(dialog);
    }

    /**
     * @deprecated call {@link #showDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    /**
     * @deprecated call {@link #showDialog(AwesomeFragment, int)} instead of this method.
     */
    @Deprecated
    @Override
    public int show(FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag);
    }

    /**
     * Present the fragment as dialog
     */
    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        dialog.setTargetFragment(this, requestCode);
        dialog.show(getFragmentManager(), dialog.getSceneId());
    }

    /**
     * set the animation for dialog
     *
     * @param type animation type
     */
    public void setAnimationType(AnimationType type) {
        Bundle args = FragmentHelper.getArguments(this);
        args.putString(ARGS_ANIMATION_TYPE, type.name());
    }

    /**
     * get the dialog animation type
     *
     * @return dialog animation type
     */
    public AnimationType getAnimationType() {
        Bundle args = FragmentHelper.getArguments(this);
        String animationType = args.getString(ARGS_ANIMATION_TYPE);
        if (animationType == null) {
            return AnimationType.None;
        }
        return AnimationType.valueOf(animationType);
    }

    private void animateIn() {
        View root = getView();
        if (root == null || !(root instanceof DialogFrameLayout)) {
            return;
        }
        AnimationType type = getAnimationType();
        boolean shouldAnimated = type != AnimationType.None;

        if (!shouldAnimated) {
            Bundle args = FragmentHelper.getArguments(this);
            String animationType = args.getString(ARGS_ANIMATION_TYPE);
            if (animationType == null) {
                DialogFrameLayout frameLayout = (DialogFrameLayout) root;
                View contentView = frameLayout.getChildAt(0);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                if (layoutParams.gravity == Gravity.BOTTOM) {
                    shouldAnimated = true;
                    type = AnimationType.Slide;
                    setAnimationType(type);
                }
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

    private void animateOut() {
        View root = getView();
        AnimationType type = getAnimationType();
        boolean shouldAnimated = type != AnimationType.None && root != null && root instanceof DialogFrameLayout;
        if (shouldAnimated) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            if (type == AnimationType.Slide) {
                animateDownOut(contentView);
            }
        }
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

    private void animateDownOut(@NonNull final View contentView) {
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
        set.setAnimationListener(createAnimationListener(contentView));
        contentView.startAnimation(set);
    }

    private Animation.AnimationListener createAnimationListener(@NonNull final View animationView) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animatingOut = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animatingOut = false;
                /**
                 * Bugfix： Attempting to destroy the window while drawing!
                 */
                animationView.post(new Runnable() {
                    @Override
                    public void run() {
                        // java.lang.IllegalArgumentException: View=com.android.internal.policy.PhoneWindow$DecorView{22dbf5b V.E...... R......D 0,0-1080,1083} not attached to window manager
                        // 在dismiss的时候可能已经detach了，简单try-catch一下
                        try {
                            dismiss();
                        } catch (Exception e) {
                            Log.w(TAG, "dismiss error\n" + Log.getStackTraceString(e));
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    // ------ NavigationFragment -----
    @Nullable
    public NavigationFragment getNavigationFragment() {
        if (this instanceof NavigationFragment) {
            return (NavigationFragment) this;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && !parent.getShowsDialog()) {
            return parent.getNavigationFragment();
        }
        return null;
    }

    public boolean isNavigationRoot() {
        NavigationFragment navigationFragment = getNavigationFragment();
        if (navigationFragment != null) {
            AwesomeFragment awesomeFragment = navigationFragment.getRootFragment();
            return awesomeFragment == this;
        }
        return false;
    }

    protected boolean shouldHideBackButton() {
        return false;
    }

    protected boolean backInteractive() {
        return true;
    }

    protected boolean hidesBottomBarWhenPushed() {
        return true;
    }

    boolean shouldHideTabBarWhenPushed() {
        NavigationFragment navigationFragment = getNavigationFragment();
        return navigationFragment != null && navigationFragment.getRootFragment().hidesBottomBarWhenPushed();
    }

    void handleHideBottomBarWhenPushed(int transit, boolean enter, PresentAnimation animation) {
        // handle hidesBottomBarWhenPushed
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof NavigationFragment) {
            NavigationFragment navigationFragment = (NavigationFragment) parent;
            TabBarFragment tabBarFragment = navigationFragment.getTabBarFragment();
            if (tabBarFragment == null || !enter) {
                return;
            }

            int index = getIndexAtBackStack();
            if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
                if (index == 0) {
                    if (tabBarFragment.getTabBar() != null) {
                        tabBarFragment.getTabBar().setVisibility(View.VISIBLE);
                    }

                } else if (index == 1 && shouldHideTabBarWhenPushed()) {
                    tabBarFragment.hideTabBarWhenPush(animation.exit);
                }
            } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
                if (index == 0 && shouldHideTabBarWhenPushed()) {
                    tabBarFragment.showTabBarWhenPop(animation.popEnter);
                }
            }
        }
    }

    private volatile AwesomeToolbar toolbar;

    public AwesomeToolbar getAwesomeToolbar() {
        return toolbar;
    }

    private void handleNavigationFragmentStuff(@NonNull View root) {
        AwesomeFragment parent = getParentAwesomeFragment();
        boolean hasNavigationParent = parent != null && (parent instanceof NavigationFragment);
        if (hasNavigationParent) {
            // create toolbar if needed
            createToolbarIfNeeded(root);
            adjustBottomPaddingIfNeeded(root);
        }
    }

    private void createToolbarIfNeeded(@NonNull View root) {
        AwesomeToolbar toolbar = onCreateAwesomeToolbar(root);
        if (toolbar != null) {
            customAwesomeToolbar(toolbar);
        }
        this.toolbar = toolbar;
    }

    private void adjustBottomPaddingIfNeeded(final View root) {
        int index = getIndexAtBackStack();
        if (index == 0 || !shouldHideTabBarWhenPushed()) {
            int color = Color.parseColor(style.getTabBarBackgroundColor());
            if (Color.alpha(color) == 255) {
                final TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                    root.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tabBarFragment.getTabBar().getHeight() != 0) {
                                bottomPadding = tabBarFragment.getTabBar().getHeight();
                                root.setPadding(0, 0, 0, bottomPadding);
                            } else {
                                root.setPadding(0, 0, 0, bottomPadding);
                            }
                        }
                    });
                }
            }
        }
    }

    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        if (getView() == null || getContext() == null) return null;

        int height = getToolbarHeight();
        AwesomeToolbar toolbar = new AwesomeToolbar(getContext());
        if (parent instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) parent;
            linearLayout.addView(toolbar, 0, new LinearLayout.LayoutParams(-1, height));
        } else if (parent instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) parent;
            frameLayout.addView(toolbar, new FrameLayout.LayoutParams(-1, height));
        } else {
            throw new UnsupportedOperationException("AwesomeFragment 无法为 " + parent.getClass().getSimpleName()
                    + " 添加 Toolbar. 请重写 onCreateAwesomeToolbar 并返回 null, 这样你就可以自行添加 Toolbar 了。");
        }

        if (isStatusBarTranslucent()) {
            appendStatusBarPadding(toolbar, getToolbarHeight());
        }

        return toolbar;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(style.getElevation());
        } else {
            toolbar.setShadow(style.getShadow());
        }
        if (!isNavigationRoot() && !shouldHideBackButton()) {
            toolbar.setNavigationIcon(style.getBackIcon());
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavigationFragment navigationFragment = getNavigationFragment();
                    if (navigationFragment != null) {
                        getNavigationFragment().dispatchBackPressed();
                    }
                }
            });
        }
    }

    protected int preferredToolbarColor() {
        AwesomeFragment childFragmentForToolbarColor = childFragmentForAppearance();
        if (childFragmentForToolbarColor != null) {
            return childFragmentForToolbarColor.preferredToolbarColor();
        }
        return style.getToolbarBackgroundColor();
    }

    public void setNeedsToolbarAppearanceUpdate() {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.setBackgroundColor(preferredToolbarColor());
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

    public void setLeftBarButtonItems(ToolbarButtonItem[] barButtonItems) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.clearLeftButtons();
            if (barButtonItems == null) {
                if (!isNavigationRoot() && !shouldHideBackButton()) {
                    toolbar.setNavigationIcon(style.getBackIcon());
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NavigationFragment navigationFragment = getNavigationFragment();
                            if (navigationFragment != null) {
                                navigationFragment.popFragment();
                            }
                        }
                    });
                }
                return;
            }

            for (ToolbarButtonItem barButtonItem : barButtonItems) {
                Drawable drawable = drawableFromBarButtonItem(barButtonItem);
                toolbar.addLeftButton(drawable, barButtonItem.title, barButtonItem.tintColor, barButtonItem.enabled, barButtonItem.onClickListener);
            }
        }
    }

    public void setRightBarButtonItems(ToolbarButtonItem[] barButtonItems) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            toolbar.clearRightButtons();
            if (barButtonItems == null) {
                return;
            }
            for (ToolbarButtonItem barButtonItem : barButtonItems) {
                Drawable drawable = drawableFromBarButtonItem(barButtonItem);
                toolbar.addRightButton(drawable, barButtonItem.title, barButtonItem.tintColor, barButtonItem.enabled, barButtonItem.onClickListener);
            }
        }
    }

    private ToolbarButtonItem leftToolbarButtonItem;

    public void setLeftBarButtonItem(ToolbarButtonItem barButtonItem) {
        leftToolbarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                toolbar.clearLeftButton();
                if (!isNavigationRoot() && !shouldHideBackButton()) {
                    toolbar.setNavigationIcon(style.getBackIcon());
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NavigationFragment navigationFragment = getNavigationFragment();
                            if (navigationFragment != null) {
                                getNavigationFragment().dispatchBackPressed();
                            }
                        }
                    });
                }
                return;
            }
            Drawable drawable = drawableFromBarButtonItem(barButtonItem);
            toolbar.setLeftButton(drawable, barButtonItem.title, barButtonItem.tintColor, barButtonItem.enabled, barButtonItem.onClickListener);
            barButtonItem.attach(toolbar.getLeftButton());
        }
    }

    public ToolbarButtonItem getLeftToolbarButtonItem() {
        return leftToolbarButtonItem;
    }

    private ToolbarButtonItem rightToolbarButtonItem;

    public void setRightBarButtonItem(ToolbarButtonItem barButtonItem) {
        rightToolbarButtonItem = barButtonItem;
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                toolbar.clearRightButton();
                return;
            }
            Drawable drawable = drawableFromBarButtonItem(barButtonItem);
            toolbar.setRightButton(drawable, barButtonItem.title, barButtonItem.tintColor, barButtonItem.enabled, barButtonItem.onClickListener);
            barButtonItem.attach(toolbar.getRightButton());
        }
    }

    public ToolbarButtonItem getRightToolbarButtonItem() {
        return rightToolbarButtonItem;
    }

    private Drawable drawableFromBarButtonItem(ToolbarButtonItem barButtonItem) {
        if (getContext() == null) {
            return null;
        }
        Drawable drawable = null;
        if (barButtonItem.iconUri != null) {
            drawable = DrawableUtils.fromUri(getContext(), barButtonItem.iconUri);
        } else if (barButtonItem.iconRes != 0) {
            drawable = getContext().getResources().getDrawable(barButtonItem.iconRes);
        }
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
        }
        return drawable;
    }

    // ------ TabBarFragment -------

    @Nullable
    public TabBarFragment getTabBarFragment() {
        if (this instanceof TabBarFragment) {
            return (TabBarFragment) this;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && !parent.getShowsDialog()) {
            return parent.getTabBarFragment();
        }
        return null;
    }

    private TabBarItem tabBarItem;

    public void setTabBarItem(TabBarItem item) {
        tabBarItem = item;
        Bundle args = FragmentHelper.getArguments(this);
        args.putParcelable(ARGS_TAB_BAR_ITEM, tabBarItem);
    }

    public TabBarItem getTabBarItem() {
        if (tabBarItem == null) {
            Bundle args = FragmentHelper.getArguments(this);
            tabBarItem = args.getParcelable(ARGS_TAB_BAR_ITEM);
        }
        return tabBarItem;
    }

    // ------ DrawerFragment -------

    @Nullable
    public DrawerFragment getDrawerFragment() {
        if (this instanceof DrawerFragment) {
            return (DrawerFragment) this;
        }
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null && !parent.getShowsDialog()) {
            return parent.getDrawerFragment();
        }
        return null;
    }

}
