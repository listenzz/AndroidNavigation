package me.listenzz.navigation;

import android.app.Activity;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = super.onGetLayoutInflater(savedInstanceState);
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

    protected void onCustomStyle(Style style) {

    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        if (!isParentFragment()) {
            setBackgroundDrawable(root, new ColorDrawable(style.getScreenBackgroundColor()));
        }
        if (getDialog() == null && getParentAwesomeFragment() == null) {
            fixKeyboardBugAtKitkat(root, isStatusBarTranslucent());
        }
        handleNavigationFragmentStuff(root);
    }

    @Override
    public void onDestroyView() {
        if (getView() != null && getDialog() == null) {
            AppUtils.hideSoftInput(getView());
        }
        super.onDestroyView();
    }

    private void setBackgroundDrawable(View root, Drawable drawable) {
        if (getDialog() == null) {
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

    public void presentFragment(AwesomeFragment fragment, int requestCode) {
        if (presentableActivity != null) {
            Bundle args = FragmentHelper.getArguments(fragment);
            args.putInt(ARGS_REQUEST_CODE, requestCode);
            presentableActivity.presentFragment(fragment);
        }
    }

    public void dismissFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
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

    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        //Log.i(TAG, toString() + "#onFragmentResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
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
        return style.getStatusBarColor();
    }

    protected boolean preferredStatusBarColorAnimated() {
        AwesomeFragment childFragmentForStatusBarColor = childFragmentForAppearance();
        if (childFragmentForStatusBarColor != null) {
            return childFragmentForStatusBarColor.preferredStatusBarColorAnimated();
        }
        return style.isStatusBarColorAnimated();
    }

    protected AwesomeFragment childFragmentForAppearance() {
        return null;
    }

    public void setNeedsStatusBarAppearanceUpdate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        // Log.i(TAG, getDebugTag() + "#setNeedsStatusBarAppearanceUpdate");
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            parent.setNeedsStatusBarAppearanceUpdate();
        } else {

            // statusBarHidden
            setStatusBarHidden(preferredStatusBarHidden());

            // statusBarStyle
            setStatusBarStyle(preferredStatusBarStyle());

            // statusBarColor
            boolean shouldAdjustForWhiteStatusBar = !AppUtils.isBlackColor(preferredStatusBarColor(), 176);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !AppUtils.isMiuiV6()) {
                shouldAdjustForWhiteStatusBar = shouldAdjustForWhiteStatusBar && preferredStatusBarStyle() == BarStyle.LightContent;
            }
            int color = preferredStatusBarColor();
            if (shouldAdjustForWhiteStatusBar) {
                color = Color.parseColor("#B0B0B0");
            }
            if (isStatusBarTranslucent() && color == preferredToolbarColor()) {
                color = Color.TRANSPARENT;
            }
            setStatusBarColor(color, preferredStatusBarColorAnimated());

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

    public Window getWindow() {
        if (getDialog() != null) {
            return getDialog().getWindow();
        }

        if (getActivity() != null) {
            return getActivity().getWindow();
        }

        return null;
    }

    public void setStatusBarTranslucent(boolean translucent) {
        if (getDialog() != null) {
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

    // ------ NavigationFragment -----

    public NavigationFragment getNavigationFragment() {
        if (this instanceof NavigationFragment) {
            return (NavigationFragment) this;
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

    boolean shouldHideBottomBarWhenPushed() {
        NavigationFragment navigationFragment = getNavigationFragment();
        return navigationFragment.getRootFragment().hidesBottomBarWhenPushed();
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
                    tabBarFragment.getBottomBar().setVisibility(View.VISIBLE);
                } else if (index == 1 && shouldHideBottomBarWhenPushed()) {
                    tabBarFragment.hideBottomNavigationBarAnimatedWhenPush(animation.exit);
                }
            } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
                if (index == 0 && shouldHideBottomBarWhenPushed()) {
                    tabBarFragment.showBottomNavigationBarAnimatedWhenPop(animation.popEnter);
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
        if (index == 0 || !shouldHideBottomBarWhenPushed()) {
            int color = Color.parseColor(style.getBottomBarBackgroundColor());
            if (Color.alpha(color) == 255) {
                final TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null) {
                    root.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tabBarFragment.getBottomBar().getHeight() != 0) {
                                bottomPadding = tabBarFragment.getBottomBar().getHeight();
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
        return  AppUtils.dp2px(requireContext(), style.getToolbarHeight());
    }

    private void customAwesomeToolbar(AwesomeToolbar toolbar) {
        toolbar.setBackgroundColor(preferredToolbarColor());
        toolbar.setButtonTintColor(style.getToolbarButtonTintColor());
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
                    getNavigationFragment().dispatchBackPressed();
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
                            getNavigationFragment().popFragment();
                        }
                    });
                }
                return;
            }

            for (ToolbarButtonItem barButtonItem : barButtonItems) {
                Drawable drawable = drawableFromBarButtonItem(barButtonItem);
                toolbar.addLeftButton(drawable, barButtonItem.title, barButtonItem.enabled, barButtonItem.onClickListener);
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
                toolbar.addRightButton(drawable, barButtonItem.title, barButtonItem.enabled, barButtonItem.onClickListener);
            }
        }
    }

    public void setLeftBarButtonItem(ToolbarButtonItem barButtonItem) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                if (!isNavigationRoot() && !shouldHideBackButton()) {
                    toolbar.setNavigationIcon(style.getBackIcon());
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getNavigationFragment().dispatchBackPressed();
                        }
                    });
                }
                return;
            }
            Drawable drawable = drawableFromBarButtonItem(barButtonItem);
            toolbar.setLeftButton(drawable, barButtonItem.title, barButtonItem.enabled, barButtonItem.onClickListener);
        }
    }

    public void setRightBarButtonItem(ToolbarButtonItem barButtonItem) {
        AwesomeToolbar toolbar = getAwesomeToolbar();
        if (toolbar != null) {
            if (barButtonItem == null) {
                return;
            }
            Drawable drawable = drawableFromBarButtonItem(barButtonItem);
            toolbar.setRightButton(drawable, barButtonItem.title, barButtonItem.enabled, barButtonItem.onClickListener);
        }
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
