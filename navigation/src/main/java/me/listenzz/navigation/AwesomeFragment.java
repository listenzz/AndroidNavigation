package me.listenzz.navigation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.listenzz.navigation.adapter.FlymeOSStatusBarColorUtils;
import me.listenzz.navigation.adapter.MiuiOSStatusBarColorUtils;
import me.listenzz.navigation.adapter.SoftInputLayoutListener;

/**
 * Created by Listen on 2018/1/11.
 */

public abstract class AwesomeFragment extends DialogFragment implements FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "Navigation";

    public static final String ARGS_SCENE_ID = "sceneId";
    public static final String ARGS_REQUEST_CODE = "requestCode";
    public static final String ARGS_ANIMATION = "animation";
    public static final String ARGS_TAB_BAR_ITEM = "tab_bar_item";

    // ------- lifecycle methods -------

    private PresentableActivity presentableActivity;

    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);

    protected Style style;

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
        getChildFragmentManager().addOnBackStackChangedListener(this);
        // Log.i(TAG, getDebugTag() + "#onCreate");
    }

    @Override
    public void onDestroy() {
        getChildFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        if (style == null) {
            try {
                style = presentableActivity.getStyle().clone();
                onCustomStyle(style);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                style = presentableActivity.getStyle();
            }
        }

        if (!isParentFragment()) {
            setBackgroundDrawable(root, new ColorDrawable(style.getScreenBackgroundColor()));
        }

        AwesomeFragment parent = getParent();
        boolean shouldAutoCreateToolbar = parent != null && (parent instanceof NavigationFragment);
        if (shouldAutoCreateToolbar) {
            Toolbar toolbar = onCreateToolbar(getView());
            if (toolbar != null && !isNavigationRoot() && !shouldHideBackButton()) {
                toolbar.setNavigationIcon(style.getBackIcon());
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getNavigationFragment().popFragment();
                    }
                });
            }
            this.toolbar = toolbar;
        }
        // Log.i(TAG, getDebugTag() + "#onViewCreated");
    }

    @Override
    public void onDestroyView() {
        AppUtils.hideSoftInput(getView());
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Log.i(TAG, getDebugTag() + "#onActivityCreated");
    }

    private void setBackgroundDrawable(View root, Drawable drawable) {
        if (getDialog() == null) {
            root.setBackground(drawable);
            getWindow().setBackgroundDrawable(null);
        }
    }

    protected void onCustomStyle(Style style) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (childFragmentForAppearance() == null) {
            // Log.w(TAG, getDebugTag() + "#onResume-");
            setNeedsStatusBarAppearanceUpdate();
        }
        // Log.w(TAG, getDebugTag() + "#onResume");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isAdded()) {
            if (childFragmentForAppearance() == null) {
                // Log.w(TAG, getDebugTag() + "#onHiddenChanged:-");
                setNeedsStatusBarAppearanceUpdate();
            }
        }

        List<AwesomeFragment> fragments = getAddedChildFragments();
        for (AwesomeFragment fragment : fragments) {
            fragment.onHiddenChanged(hidden);
        }

        //Log.w(TAG, getDebugTag() + "#onHiddenChanged:" + hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            // Log.w(TAG, getDebugTag() + "#isVisibleToUser:-");
            setNeedsStatusBarAppearanceUpdate();
        }
        //Log.w(TAG, getDebugTag() + "#isVisibleToUser:" + isVisibleToUser);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        PresentAnimation animation = getAnimation();

        handleHideBottomBarWhenPushed(transit, enter, animation);
        // ---------
        Log.d(TAG, getDebugTag() + "  " + animation.name() + " transit:" + transit + " enter:" + enter);

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

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, getClass().getSimpleName() + " Entry index:" + entry.getId() + " tag:" + entry.getName());
        }
    }

    // ------ lifecycle arch -------

    protected void scheduleTask(Runnable runnable) {
        lifecycleDelegate.scheduleTask(runnable);
    }

    protected boolean isAtLeastStarted() {
        return lifecycleDelegate.isAtLeastStarted();
    }

    protected boolean isAtLeastCreated() {
        return lifecycleDelegate.isAtLeastCreated();
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
        AwesomeFragment parent = getParent();
        if (parent != null) {
            parent.setResult(resultCode, result);
            parent.dismissFragment();
            return;
        }

        if (presentableActivity != null) {
            presentableActivity.dismissFragment(this);
        }
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
            List<AwesomeFragment> fragments = getAddedChildFragments();
            for (AwesomeFragment child : fragments) {
                child.onFragmentResult(requestCode, resultCode, data);
            }
        }
    }

    public void addFragment(final int containerId, final AwesomeFragment fragment, final PresentAnimation animation) {
        if (isAtLeastStarted()) {
            executeAddFragment(containerId, fragment, animation);
        } else {
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executeAddFragment(containerId, fragment, animation);
                }
            });
        }
    }

    private void executeAddFragment(int containerId, AwesomeFragment fragment, PresentAnimation animation) {
        FragmentHelper.addFragment(getChildFragmentManager(), containerId, fragment, animation);
    }

    boolean dispatchBackPressed() {
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

    public AwesomeFragment getPresentedFragment() {
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getPresentedFragment();
        }
        return presentableActivity.getPresentedFragment(this);
    }

    public AwesomeFragment getPresentingFragment() {
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getPresentingFragment();
        }
        return presentableActivity.getPresentingFragment(this);
    }

    public AwesomeFragment getInnermostFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null) {
            AwesomeFragment child = (AwesomeFragment) fragment;
            return child.getInnermostFragment();
        } else if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            return child.getInnermostFragment();
        }
        return this;
    }

    public String getDebugTag() {
        if (getActivity() == null) {
            return null;
        }
        AwesomeFragment parent = getParent();
        if (parent == null) {
            return "#" + indexInAddedList() + "-" + getClass().getSimpleName();
        } else {
            return parent.getDebugTag() + "#" + indexInAddedList() + "-" + getClass().getSimpleName();
        }
    }

    protected int indexAtBackStack() {
        return FragmentHelper.findIndexAtBackStack(getFragmentManager(), this);
    }

    protected int getChildFragmentCountAtBackStack() {
        FragmentManager fragmentManager = getChildFragmentManager();
        return fragmentManager.getBackStackEntryCount();
    }

    protected int indexInAddedList() {
        List<Fragment> fragments = getFragmentManager().getFragments();
        return fragments.indexOf(this);
    }

    protected AwesomeFragment getParent() {
        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    protected List<AwesomeFragment> getAddedChildFragments() {
        List<AwesomeFragment> children = new ArrayList<>();
        if (isAdded()) {
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            for (int i = 0, size = fragments.size(); i < size; i++) {
                children.add((AwesomeFragment) fragments.get(i));
            }
        }
        return children;
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

    protected boolean isParentFragment() {
        return false;
    }

    // ------- statusBar --------

    protected BarStyle preferredStatusBarStyle() {
        AwesomeFragment childFragmentForStatusBarStyle = childFragmentForAppearance();
        if (childFragmentForStatusBarStyle != null) {
            return childFragmentForStatusBarStyle.preferredStatusBarStyle();
        }
        return style.getToolbarStyle();
    }

    protected boolean preferredStatusBarHidden() {
        AwesomeFragment childFragmentForStatusBarHidden = childFragmentForAppearance();
        if (childFragmentForStatusBarHidden != null) {
            return childFragmentForStatusBarHidden.preferredStatusBarHidden();
        }
        return false;
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
        return false;
    }

    protected int preferredToolbarBackgroundColor() {
        AwesomeFragment childFragmentForAppearance = childFragmentForAppearance();
        if (childFragmentForAppearance != null) {
            return childFragmentForAppearance.preferredToolbarBackgroundColor();
        }
        if (toolbar == null) {
            return Color.TRANSPARENT;
        } else {
            return style.getToolbarBackgroundColor();
        }
    }

    protected AwesomeFragment childFragmentForAppearance() {
        return null;
    }

    public void setNeedsStatusBarAppearanceUpdate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        // Log.i(TAG, getDebugTag() + " #setNeedsStatusBarAppearanceUpdate");

        AwesomeFragment parent = getParent();
        if (parent != null) {
            parent.setNeedsStatusBarAppearanceUpdate();
        } else {
            // statusBarHidden
            setStatusBarHidden(preferredStatusBarHidden());

            // statusBarStyle
            setStatusBarStyle(preferredStatusBarStyle());

            // statusBarColor
            boolean shouldAdjustForWhiteStatusBar;
            if (isContentUnderStatusBar()) {
                shouldAdjustForWhiteStatusBar = preferredStatusBarColor() == Color.TRANSPARENT && preferredToolbarBackgroundColor() == Color.WHITE;
            } else {
                shouldAdjustForWhiteStatusBar = preferredStatusBarColor() == Color.WHITE;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldAdjustForWhiteStatusBar = shouldAdjustForWhiteStatusBar && preferredStatusBarStyle() == BarStyle.LightContent;
            }

            if (shouldAdjustForWhiteStatusBar) {
                int color = Color.parseColor("#B0B0B0");
                setStatusBarColor(color, preferredStatusBarColorAnimated());
            } else {
                setStatusBarColor(preferredStatusBarColor(), preferredStatusBarColorAnimated());
            }
        }
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

    protected void setStatusBarStyle(BarStyle barStyle) {
        if (AppUtils.isMiuiV6()) {
            MiuiOSStatusBarColorUtils.setStatusBarDarkMode(barStyle == BarStyle.DarkContent, getWindow());
        } else if (AppUtils.isFlymeV4()) {
            AwesomeFragment childFragmentForBarStyle = childFragmentForAppearance();
            Style style = this.style;
            while (childFragmentForBarStyle != null) {
                style = childFragmentForBarStyle.style;
                Log.w(TAG, childFragmentForBarStyle.getDebugTag() + " color:" + style.getToolbarTintColor());
                childFragmentForBarStyle = childFragmentForBarStyle.childFragmentForAppearance();
            }

            style.setToolbarStyle(barStyle);

            if (getDialog() == null) {
                FlymeOSStatusBarColorUtils.setStatusBarDarkIcon(getActivity(), style.getToolbarTintColor());
            } else {
                FlymeOSStatusBarColorUtils.setStatusBarDarkIcon(getWindow(), style.getToolbarTintColor());
            }

        } else {
            AppUtils.setStatusBarStyle(getWindow(), barStyle == BarStyle.DarkContent);
        }
    }

    protected void setContentUnderStatusBar(boolean under) {
        if (getDialog() != null) {
            AppUtils.setStatusBarTranslucent(getWindow(), under);
        } else {
            presentableActivity.setContentUnderStatusBar(under);
        }
    }

    protected void onContentUnderStatusBar(boolean under) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            if (under) {
                appendStatusBarPadding(toolbar, getToolbarHeight());
            } else {
                removeStatusBarPadding(toolbar, getToolbarHeight());
            }
        }

        List<AwesomeFragment> children = getAddedChildFragments();
        for (int i = 0, size = children.size(); i < size; i++) {
            AwesomeFragment child = children.get(i);
            child.onContentUnderStatusBar(under);
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    public void fixKeyboardBug(View root, boolean isContentUnderStatusBar) {
        if (isContentUnderStatusBar) {
            if (globalLayoutListener == null) {
                globalLayoutListener = new SoftInputLayoutListener(root);
                root.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
            }
        } else {
            if (globalLayoutListener != null) {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
                globalLayoutListener = null;
            }
        }
    }

    public void fixKeyboardBugAtKitkat(View root, boolean isContentUnderStatusBar) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            fixKeyboardBug(root, isContentUnderStatusBar);
        }
    }

    protected boolean isContentUnderStatusBar() {
        return presentableActivity.isContentUnderStatusBar();
    }

    protected void setStatusBarHidden(boolean hidden) {
        AppUtils.setStatusBarHidden(getWindow(), hidden);
    }

    protected void setStatusBarColor(int color, boolean animated) {
        AppUtils.setStatusBarColor(getWindow(), color, animated);
    }

    protected void appendStatusBarPadding(View view, int viewHeight) {
        AppUtils.appendStatusBarPadding(view, viewHeight);
    }

    protected void removeStatusBarPadding(View view, int viewHeight) {
        AppUtils.removeStatusBarPadding(view, viewHeight);
    }

    protected int getToolbarHeight() {
        return AppUtils.fetchContextDimension(getContext(), R.attr.actionBarSize);
    }

    // ------ NavigationFragment -----

    public NavigationFragment getNavigationFragment() {
        if (this instanceof NavigationFragment) {
            return (NavigationFragment) this;
        }
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getNavigationFragment();
        }
        return null;
    }

    protected boolean isNavigationRoot() {
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
            if (tabBarFragment != null) {
                int index = indexAtBackStack();
                if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
                    if (enter) {
                        if (index == 0) {
                            tabBarFragment.getBottomBar().setVisibility(View.VISIBLE);
                        } else if (index == 1 && shouldHideBottomBarWhenPushed()) {
                            tabBarFragment.hideBottomNavigationBarAnimatedWhenPush(animation.exit);
                        }
                    }
                } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
                    if (enter && index == 0 && shouldHideBottomBarWhenPushed()) {
                        tabBarFragment.showBottomNavigationBarAnimatedWhenPop(animation.popEnter);
                    }
                }
            }
        }
    }

    private volatile Toolbar toolbar;

    public Toolbar getToolbar() {
        return toolbar;
    }

    protected Toolbar onCreateToolbar(View parent) {
        if (getView() == null || getContext() == null) return null;

        TypedValue typedValue = new TypedValue();
        int height = 0;
        if (getContext().getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
            height = (int) TypedValue.complexToDimension(typedValue.data, getContext().getResources().getDisplayMetrics());
        }

        AwesomeToolbar toolbar = new AwesomeToolbar(getContext());
        if (parent instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) parent;
            linearLayout.addView(toolbar, 0, new LinearLayout.LayoutParams(-1, height));
        } else if (parent instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) parent;
            frameLayout.addView(toolbar, new FrameLayout.LayoutParams(-1, height));
        } else {
            throw new UnsupportedOperationException("NavigationFragment 还没适配 " + parent.getClass().getSimpleName());
        }

        if (isContentUnderStatusBar()) {
            appendStatusBarPadding(toolbar, getToolbarHeight());
        }

        customAwesomeToolbar(toolbar);

        return toolbar;
    }

    private void customAwesomeToolbar(AwesomeToolbar toolbar) {
        toolbar.setBackgroundColor(style.getToolbarBackgroundColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(style.getElevation());
        } else {
            toolbar.setShadow(style.getShadow());
        }
    }

    protected void setTitle(@StringRes int resId) {
        setTitle(getContext().getText(resId));
    }

    public void setTitle(CharSequence title) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            if (toolbar instanceof AwesomeToolbar) {
                AwesomeToolbar awesomeToolbar = (AwesomeToolbar) toolbar;
                awesomeToolbar.setTitleGravity(style.getTitleGravity());
                TextView titleView = awesomeToolbar.getTitleView();
                titleView.setText(title);
                titleView.setTextColor(style.getTitleTextColor());
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, style.getTitleTextSize());
            } else {
                toolbar.setTitle(title);
            }
        }
    }

    protected void setToolbarLeftButton(@DrawableRes int icon, @StringRes int title, boolean enabled, final View.OnClickListener onClickListener) {
        setToolbarLeftButton(ContextCompat.getDrawable(getContext(), icon), getContext().getString(title), enabled, onClickListener);
    }

    protected void setToolbarLeftButton(Drawable icon, String title, boolean enabled, final View.OnClickListener onClickListener) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            if (toolbar instanceof AwesomeToolbar) {
                AwesomeToolbar awesomeToolbar = (AwesomeToolbar) toolbar;
                TextView leftButton = awesomeToolbar.getLeftButton();
                toolbar.setContentInsetsRelative(0, toolbar.getContentInsetEnd());
                toolbar.setNavigationIcon(null);
                toolbar.setNavigationOnClickListener(null);
                setAwesomeToolbarButton(awesomeToolbar, leftButton, icon, title, enabled);
                leftButton.setOnClickListener(onClickListener);
            } else {
                toolbar.setNavigationIcon(icon);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListener.onClick(v);
                    }
                });
            }
        }
    }

    protected void setToolbarRightButton(@DrawableRes int icon, @StringRes int title, boolean enabled, final View.OnClickListener onClickListener) {
        setToolbarRightButton(ContextCompat.getDrawable(getContext(), icon), getContext().getString(title), enabled, onClickListener);
    }

    protected void setToolbarRightButton(Drawable icon, String title, boolean enabled, final View.OnClickListener onClickListener) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            if (toolbar instanceof AwesomeToolbar) {
                AwesomeToolbar awesomeToolbar = (AwesomeToolbar) toolbar;
                TextView rightButton = awesomeToolbar.getRightButton();
                toolbar.setContentInsetsRelative(toolbar.getContentInsetStart(), 0);
                setAwesomeToolbarButton(awesomeToolbar, rightButton, icon, title, enabled);
                rightButton.setOnClickListener(onClickListener);
            } else {
                Menu menu = toolbar.getMenu();
                MenuItem menuItem = menu.add(title);
                menuItem.setIcon(icon);
                menuItem.setEnabled(enabled);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onClickListener.onClick(null);
                        return true;
                    }
                });
            }
        }
    }

    private void setAwesomeToolbarButton(AwesomeToolbar toolbar, TextView button, Drawable icon, String title, boolean enabled) {
        button.setOnClickListener(null);
        button.setText(null);
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        button.setMaxWidth(Integer.MAX_VALUE);
        button.setAlpha(1.0f);
        button.setVisibility(View.VISIBLE);

        int color = style.getToolbarButtonTintColor();
        if (!enabled) {
            color = DrawableUtils.generateGrayColor(color);
            button.setAlpha(0.3f);
        }
        button.setEnabled(enabled);

        if (icon != null) {
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            int width = toolbar.getContentInsetStartWithNavigation();
            int padding = (width - icon.getIntrinsicWidth()) / 2;
            button.setMaxWidth(width);
            button.setPaddingRelative(padding, 0, padding, 0);
        } else {
            int padding = toolbar.getContentInset();
            button.setPaddingRelative(padding, 0, padding, 0);
            button.setText(title);
            button.setTextColor(color);
            button.setTextSize(style.getToolbarButtonTextSize());
        }

        TypedValue typedValue = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.actionBarItemBackground, typedValue, true)) {
            button.setBackgroundResource(typedValue.resourceId);
        }
    }

    // ------ TabBarFragment -------

    public TabBarFragment getTabBarFragment() {
        if (this instanceof TabBarFragment) {
            return (TabBarFragment) this;
        }
        AwesomeFragment parent = getParent();
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
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getDrawerFragment();
        }
        return null;
    }

}
