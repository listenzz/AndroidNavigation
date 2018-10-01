package me.listenzz.navigation;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class NavigationFragment extends AwesomeFragment implements SwipeBackLayout.SwipeListener {

    private SwipeBackLayout swipeBackLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root;
        if (style.isSwipeBackEnabled()) {
            root = inflater.inflate(R.layout.nav_fragment_navigation_swipe_back, container, false);
            swipeBackLayout = root.findViewById(R.id.navigation_content);
            swipeBackLayout.setSwipeListener(this);
        } else {
            root = inflater.inflate(R.layout.nav_fragment_navigation, container, false);
        }
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            if (rootFragment == null) {
                throw new IllegalArgumentException("必须通过 `setRootFragment` 指定 rootFragment");
            } else {
                setRootFragmentInternal(rootFragment);
            }
        }
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getTopFragment();
    }

    @Override
    protected boolean onBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 1) {
            AwesomeFragment topFragment = getTopFragment();
            if (topFragment.isBackInteractive()) {
                popFragment();
            }
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    private AwesomeFragment rootFragment;

    public void setRootFragment(@NonNull final AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("NavigationFragment 已经出于 added 状态，不可以再设置 rootFragment");
        }
        this.rootFragment = fragment;
    }

    public AwesomeFragment getRootFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    private void setRootFragmentInternal(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.None);
    }

    public void pushFragment(@NonNull final AwesomeFragment fragment) {
        pushFragment(fragment, true);
    }

    public void pushFragment(@NonNull final AwesomeFragment fragment, final boolean animated) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                fragment.setTransitionAnimationListener(new StatusBarAnimationListener(fragment, getTopFragment()));
                pushFragmentInternal(fragment, animated);
            }
        });
    }

    private void pushFragmentInternal(AwesomeFragment fragment, boolean animated) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, animated ? PresentAnimation.Push : PresentAnimation.None);
    }

    public void popToFragment(@NonNull final AwesomeFragment fragment) {
        popToFragment(fragment, true);
    }

    public void popToFragment(@NonNull final AwesomeFragment fragment, final boolean animated) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                fragment.setTransitionAnimationListener(new StatusBarAnimationListener(fragment, getTopFragment()));
                popToFragmentInternal(fragment, animated);
            }
        });
    }

    private void popToFragmentInternal(AwesomeFragment fragment, boolean animated) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == fragment) {
            return;
        }

        topFragment.setAnimation(animated ? PresentAnimation.Push : PresentAnimation.None);
        fragment.setAnimation(animated ? PresentAnimation.Push : PresentAnimation.None);
        topFragment.setUserVisibleHint(false);
        fragmentManager.popBackStack(fragment.getSceneId(), 0);
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        fragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());

    }

    public void popFragment() {
        popFragment(true);
    }

    public void popFragment(boolean animated) {
        AwesomeFragment after = FragmentHelper.getLatterFragment(getChildFragmentManager(), getTopFragment());
        if (after != null) {
            popToFragment(this, animated);
            return;
        }

        AwesomeFragment before = FragmentHelper.getAheadFragment(getChildFragmentManager(), getTopFragment());
        if (before != null) {
            popToFragment(before, animated);
        }
    }

    public void popToRootFragment() {
        popToRootFragment(true);
    }

    public void popToRootFragment(boolean animated) {
        AwesomeFragment awesomeFragment = getRootFragment();
        if (awesomeFragment != null) {
            popToFragment(getRootFragment(), animated);
        }
    }

    public void replaceFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                replaceFragmentInternal(fragment);
            }
        });
    }

    private void replaceFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(fragmentManager, topFragment);
        topFragment.setAnimation(PresentAnimation.Fade);
        topFragment.setUserVisibleHint(false);
        if (aheadFragment != null) {
            aheadFragment.setAnimation(PresentAnimation.Fade);
        }
        fragmentManager.popBackStack();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (aheadFragment != null) {
            transaction.hide(aheadFragment);
        }
        fragment.setAnimation(PresentAnimation.None);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void replaceToRootFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                replaceToRootFragmentInternal(fragment);
            }
        });
    }

    private void replaceToRootFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment rootFragment = getRootFragment();
        topFragment.setAnimation(PresentAnimation.Fade);
        rootFragment.setAnimation(PresentAnimation.Fade);
        topFragment.setUserVisibleHint(false);
        fragmentManager.popBackStack(rootFragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragment.setAnimation(PresentAnimation.None);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void setChildFragments(List<AwesomeFragment> fragments) {
        // TODO
        // 弹出所有旧的 fragment

        // 添加所有新的 fragment
    }

    public AwesomeFragment getTopFragment() {
        if (isAdded()) {
            return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.navigation_content);
        }
        return null;
    }

    @Nullable
    @Override
    public NavigationFragment getNavigationFragment() {
        NavigationFragment navF = super.getNavigationFragment();
        if (navF != null) {
            AwesomeFragment parent = navF.getParentAwesomeFragment();
            while (parent != null) {
                if (parent instanceof NavigationFragment && parent.getWindow() == navF.getWindow()) {
                    throw new IllegalStateException("should not nest NavigationFragment in the same window.");
                }
                parent = parent.getParentAwesomeFragment();
            }
        }
        return navF;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return swipeBackLayout;
    }


    @Override
    public void onViewDragStateChanged(int state, float scrollPercent) {
        if (state == SwipeBackLayout.STATE_DRAGGING) {
            AwesomeFragment topFragment = getTopFragment();
            AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(getChildFragmentManager(), topFragment);
            if (aheadFragment != null && aheadFragment.getView() != null) {
                aheadFragment.getView().setVisibility(View.VISIBLE);

                if (shouldTransitionWithStatusBar(topFragment, aheadFragment)) {
                    AppUtils.setStatusBarColor(getWindow(), Color.TRANSPARENT, false);
                }
            }

            if (aheadFragment != null && aheadFragment == getRootFragment() && aheadFragment.shouldHideTabBarWhenPushed()) {
                TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null && tabBarFragment.getTabBar() != null && tabBarFragment.getView() != null) {
                    View tabBar = tabBarFragment.getTabBar();
                    tabBar.setDrawingCacheEnabled(true);
                    tabBar.buildDrawingCache(true);
                    if (tabBar.getMeasuredWidth() == 0) {
                        tabBar.measure(View.MeasureSpec.makeMeasureSpec(tabBarFragment.getView().getWidth(), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        tabBar.layout(0, 0, tabBar.getMeasuredWidth(), tabBar.getMeasuredHeight());
                    }
                    Bitmap bitmap = Bitmap.createBitmap(tabBar.getDrawingCache());
                    tabBar.setDrawingCacheEnabled(false);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                    bitmapDrawable.setBounds(0, tabBarFragment.getView().getHeight() - tabBar.getMeasuredHeight(),
                            tabBar.getMeasuredWidth(), tabBarFragment.getView().getHeight());
                    swipeBackLayout.setTabBar(bitmapDrawable);
                }
            }

        } else if (state == SwipeBackLayout.STATE_IDLE) {

            AwesomeFragment topFragment = getTopFragment();
            AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(getChildFragmentManager(), topFragment);
            if (aheadFragment != null && aheadFragment.getView() != null) {
                aheadFragment.getView().setVisibility(View.GONE);

                if (shouldTransitionWithStatusBar(topFragment, aheadFragment)) {
                    AppUtils.setStatusBarColor(getWindow(), topFragment.preferredStatusBarColor(), false);
                }
            }
            if (aheadFragment != null && scrollPercent >= 1.0f) {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);
                topFragment.setAnimation(PresentAnimation.None);
                aheadFragment.setAnimation(PresentAnimation.None);
                aheadFragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());
                fragmentManager.popBackStackImmediate(aheadFragment.getSceneId(), 0);

                if (shouldTransitionWithStatusBar(topFragment, aheadFragment)) {
                    AppUtils.setStatusBarColor(getWindow(), aheadFragment.preferredStatusBarColor(), false);
                }
            }
            swipeBackLayout.setTabBar(null);
        }
    }

    @Override
    public boolean shouldSwipeBack() {
        return style.isSwipeBackEnabled()
                && getChildFragmentCountAtBackStack() > 1
                && getTopFragment().isBackInteractive()
                && getTopFragment().isSwipeBackEnabled();
    }

    private boolean shouldTransitionWithStatusBar(AwesomeFragment topFragment, AwesomeFragment aheadFragment) {
        return isStatusBarTranslucent()
                && topFragment.preferredStatusBarColor() != aheadFragment.preferredStatusBarColor()
                && (topFragment.preferredStatusBarColor() == topFragment.preferredToolbarColor() || topFragment.preferredStatusBarColor() == Color.TRANSPARENT)
                && (aheadFragment.preferredStatusBarColor() == aheadFragment.preferredToolbarColor() || aheadFragment.preferredStatusBarColor() == Color.TRANSPARENT);
    }


    boolean shouldHandleStatusBarTransitionWhenPushed(Animation.AnimationListener listener) {
        if (listener != null && listener instanceof StatusBarAnimationListener) {
            StatusBarAnimationListener statusBarAnimationListener = (StatusBarAnimationListener) listener;
            AwesomeFragment expectFragment = statusBarAnimationListener.expectFragment;
            AwesomeFragment referredFragment = statusBarAnimationListener.referredFragment;
            return shouldTransitionWithStatusBar(expectFragment, referredFragment);
        }
        return false;
    }

    class StatusBarAnimationListener implements Animation.AnimationListener {

        AwesomeFragment expectFragment;
        AwesomeFragment referredFragment;

        StatusBarAnimationListener(@NonNull AwesomeFragment expectFragment, @NonNull AwesomeFragment referredFragment) {
            this.expectFragment = expectFragment;
            this.referredFragment = referredFragment;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (shouldTransitionWithStatusBar(expectFragment, referredFragment)) {
                AppUtils.setStatusBarColor(getWindow(), Color.TRANSPARENT, false);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (shouldTransitionWithStatusBar(expectFragment, referredFragment)) {
                expectFragment.setNeedsStatusBarAppearanceUpdate(false);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
