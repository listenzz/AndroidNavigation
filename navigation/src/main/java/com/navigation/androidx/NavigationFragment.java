package com.navigation.androidx;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

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
                throw new IllegalArgumentException("Must specify rootFragment by `setRootFragment`.");
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
        if (dragging) {
            return true;
        }

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment != null && !topFragment.isBackInteractive()) {
            return true;
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 1) {
            if (topFragment != null) {
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
            throw new IllegalStateException("NavigationFragment is at added state，can not `setRootFragment` any more.");
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

    public void pushFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        pushFragment(fragment, animated, null);
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, boolean animated, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> pushFragmentInternal(fragment, animated, completion), animated);
    }

    private void pushFragmentInternal(AwesomeFragment fragment, boolean animated, @Nullable Runnable completion) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, animated ? PresentAnimation.Push : PresentAnimation.None);
        if (completion != null) {
            completion.run();
        }
    }

    public void popToFragment(@NonNull AwesomeFragment fragment) {
        popToFragment(fragment, true);
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        popToFragment(fragment, animated, null);
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, boolean animated, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> popToFragmentInternal(fragment, animated, completion), animated);
    }

    private void popToFragmentInternal(AwesomeFragment fragment, boolean animated, @Nullable Runnable completion) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null || topFragment == fragment) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();

        topFragment.setAnimation(animated ? PresentAnimation.Push : PresentAnimation.None);
        fragment.setAnimation(animated ? PresentAnimation.Push : PresentAnimation.None);
        fragmentManager.popBackStack(fragment.getSceneId(), 0);

        fragmentManager.beginTransaction().setMaxLifecycle(fragment, Lifecycle.State.RESUMED).commit();

        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
        fragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());

        if (completion != null) {
            completion.run();
        }
    }

    public void popFragment() {
        popFragment(true);
    }

    public void popFragment(boolean animated) {
        popFragment(animated, null);
    }

    public void popFragment(boolean animated, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> popFragmentInternal(animated, completion), animated);
    }

    private void popFragmentInternal(boolean animated, @Nullable Runnable completion) {
        AwesomeFragment top = getTopFragment();
        if (top == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        AwesomeFragment previous = FragmentHelper.getFragmentBefore(top);
        if (previous != null) {
            popToFragmentInternal(previous, animated, null);
        }

        if (completion != null) {
            completion.run();
        }
    }

    public void popToRootFragment() {
        popToRootFragment(true);
    }

    public void popToRootFragment(boolean animated) {
        popToRootFragment(animated, null);
    }

    public void popToRootFragment(boolean animated, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> popToRootFragmentInternal(animated, completion), animated);
    }

    private void popToRootFragmentInternal(boolean animated, @Nullable Runnable completion) {
        AwesomeFragment awesomeFragment = getRootFragment();
        if (awesomeFragment != null) {
            popToFragmentInternal(getRootFragment(), animated, null);
        }
        if (completion != null) {
            completion.run();
        }
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment) {
        redirectToFragment(fragment, true);
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        redirectToFragment(fragment, null, animated);
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @Nullable AwesomeFragment target, boolean animated) {
        redirectToFragment(fragment, target, animated, null);
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @Nullable AwesomeFragment target, boolean animated, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> redirectToFragmentInternal(fragment, target, animated, completion), animated);
    }

    private void redirectToFragmentInternal(@NonNull AwesomeFragment fragment, @Nullable AwesomeFragment target, boolean animated, @Nullable Runnable completion) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        if (target == null) {
            target = topFragment;
        }

        AwesomeFragment aheadFragment = FragmentHelper.getFragmentBefore(target);

        topFragment.setAnimation(animated ? PresentAnimation.Redirect : PresentAnimation.Fade);
        if (aheadFragment != null && aheadFragment.isAdded()) {
            aheadFragment.setAnimation(animated ? PresentAnimation.Redirect : PresentAnimation.Fade);
        }

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();

        fragmentManager.popBackStack(target.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (aheadFragment != null && aheadFragment.isAdded()) {
            transaction.hide(aheadFragment);
            transaction.setMaxLifecycle(aheadFragment, Lifecycle.State.STARTED);
        }
        fragment.setAnimation(animated ? PresentAnimation.Push : PresentAnimation.None);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        if (completion != null) {
            completion.run();
        }
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

    private boolean dragging = false;

    @Override
    public void onViewDragStateChanged(int state, float scrollPercent) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            return;
        }

        if (state == SwipeBackLayout.STATE_DRAGGING) {
            dragging = true;
            AwesomeFragment previous = FragmentHelper.getFragmentBefore(topFragment);

            if (previous != null && previous.getView() != null) {
                previous.getView().setVisibility(View.VISIBLE);
            }

            if (previous != null && previous == getRootFragment() && previous.shouldHideTabBarWhenPushed()) {
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
            AwesomeFragment previous = FragmentHelper.getFragmentBefore(topFragment);

            if (previous != null && previous.getView() != null) {
                previous.getView().setVisibility(View.GONE);
            }

            if (previous != null && scrollPercent >= 1.0f) {
                popFragment(false);
            }

            swipeBackLayout.setTabBar(null);
            dragging = false;
        }
    }

    @Override
    public boolean shouldSwipeBack() {
        AwesomeFragment top = getTopFragment();
        if (top == null) {
            return false;
        }
        return style.isSwipeBackEnabled()
                && FragmentHelper.getBackStackEntryCount(this) > 1
                && top.isBackInteractive()
                && top.isSwipeBackEnabled();
    }

}
