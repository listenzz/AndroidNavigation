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
                rootFragment = null;
            }
        }
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Nullable
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
            if (topFragment != null && topFragment.isBackInteractive()) {
                popFragment();
            }
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    private AwesomeFragment rootFragment;

    public void setRootFragment(@NonNull AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("NavigationFragment 已经出于 added 状态，不可以再设置 rootFragment");
        }
        this.rootFragment = fragment;
    }

    @Nullable
    public AwesomeFragment getRootFragment() {
        if (isAdded()) {
            FragmentManager fragmentManager = getChildFragmentManager();
            int count = fragmentManager.getBackStackEntryCount();
            if (count > 0) {
                FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
                return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            }
        }
        return rootFragment;
    }

    private void setRootFragmentInternal(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.None);
    }

    public void pushFragment(@NonNull AwesomeFragment fragment) {
        pushFragment(fragment, true);
    }

    public void pushFragment(@NonNull final AwesomeFragment fragment, final boolean animated) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
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
                popToFragmentInternal(fragment, animated);
            }
        });
    }

    private void popToFragmentInternal(AwesomeFragment fragment, boolean animated) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null || topFragment == fragment) {
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
        AwesomeFragment top = getTopFragment();
        if (top == null) {
            return;
        }

        AwesomeFragment after = FragmentHelper.getLatterFragment(getChildFragmentManager(), top);
        if (after != null) {
            popToFragment(this, animated);
            return;
        }

        AwesomeFragment before = FragmentHelper.getAheadFragment(getChildFragmentManager(), top);
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
        if (topFragment == null) {
            return;
        }
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
        if (topFragment == null || rootFragment == null) {
            return;
        }

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

    @Nullable
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
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            return;
        }

        if (state == SwipeBackLayout.STATE_DRAGGING) {
            AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(getChildFragmentManager(), topFragment);

            if (aheadFragment != null && shouldTransitionWithStatusBar(aheadFragment, topFragment)) {
                AppUtils.setStatusBarColor(getWindow(), topFragment.preferredStatusBarColor(), false);
            }

            if (aheadFragment != null && aheadFragment.getView() != null) {
                aheadFragment.getView().setVisibility(View.VISIBLE);
            }

            if (aheadFragment != null && aheadFragment == getRootFragment() && aheadFragment.shouldHideTabBarWhenPushed()) {
                TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null && tabBarFragment.getTabBar() != null && tabBarFragment.getView() != null) {
                    View tabBar = tabBarFragment.getTabBar();
                    if (tabBar.getMeasuredWidth() == 0) {
                        tabBar.measure(View.MeasureSpec.makeMeasureSpec(tabBarFragment.getView().getWidth(), View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        tabBar.layout(0, 0, tabBar.getMeasuredWidth(), tabBar.getMeasuredHeight());
                    }
                    Bitmap bitmap = AppUtils.createBitmapFromView(tabBar);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                    bitmapDrawable.setBounds(0, tabBarFragment.getView().getHeight() - tabBar.getHeight(),
                            tabBar.getMeasuredWidth(), tabBarFragment.getView().getHeight());
                    swipeBackLayout.setTabBar(bitmapDrawable);
                }
            }

        } else if (state == SwipeBackLayout.STATE_IDLE) {
            AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(getChildFragmentManager(), topFragment);

            if (aheadFragment != null && aheadFragment.getView() != null) {
                aheadFragment.getView().setVisibility(View.GONE);
            }

            if (aheadFragment != null && scrollPercent >= 1.0f) {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);
                topFragment.setAnimation(PresentAnimation.None);
                aheadFragment.setAnimation(PresentAnimation.None);
                topFragment.setUserVisibleHint(false);
                fragmentManager.popBackStack(aheadFragment.getSceneId(), 0);
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);

                aheadFragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());
            }

            if (aheadFragment != null && shouldTransitionWithStatusBar(aheadFragment, topFragment)) {
                setNeedsStatusBarAppearanceUpdate(false);
            }

            swipeBackLayout.setTabBar(null);
        }
    }

    @Override
    public boolean shouldSwipeBack() {
        AwesomeFragment top = getTopFragment();
        if (top == null) {
            return false;
        }
        return style.isSwipeBackEnabled()
                && getChildFragmentCountAtBackStack() > 1
                && top.isBackInteractive()
                && top.isSwipeBackEnabled();
    }

    private boolean shouldTransitionWithStatusBar(AwesomeFragment aheadFragment, AwesomeFragment topFragment) {
        boolean shouldAdjustForWhiteStatusBar = AppUtils.shouldAdjustStatusBarColor(this);

        return isStatusBarTranslucent()
                && !shouldAdjustForWhiteStatusBar
                && aheadFragment.preferredStatusBarColor() != Color.TRANSPARENT
                && aheadFragment.preferredStatusBarColor() == topFragment.preferredStatusBarColor()
                && topFragment.preferredToolbarAlpha() == 255
                && aheadFragment.preferredToolbarAlpha() == 255;
    }
}
