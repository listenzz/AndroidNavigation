package com.navigation.androidx;

import static com.navigation.androidx.FragmentHelper.handleFragmentResult;

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

public class StackFragment extends AwesomeFragment implements SwipeBackLayout.SwipeListener {

    private SwipeBackLayout mSwipeBackLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mStyle.isSwipeBackEnabled()) {
            return swipeBackRootView(inflater, container);
        }
        return inflater.inflate(R.layout.nav_fragment_navigation, container, false);
    }

    @NonNull
    private View swipeBackRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View root = inflater.inflate(R.layout.nav_fragment_navigation_swipe_back, container, false);
        mSwipeBackLayout = root.findViewById(R.id.navigation_content);
        mSwipeBackLayout.setSwipeListener(this);
        int scrimAlpha = mStyle.getScrimAlpha();
        mSwipeBackLayout.setScrimColor(scrimAlpha << 24);
        return root;
    }

    @Override
    protected void performCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.performCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState == null) {
            if (mRootFragment == null) {
                throw new IllegalArgumentException("Must specify rootFragment by `setRootFragment`.");
            }
            FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, mRootFragment, TransitionAnimation.None);
            mRootFragment = null;
        }
    }

    @Override
    public boolean isLeafAwesomeFragment() {
        return false;
    }

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getTopFragment();
    }

    @Override
    protected boolean onBackPressed() {
        if (mDragging) {
            return true;
        }

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment != null && !topFragment.isBackInteractive()) {
            return true;
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 1) {
            popFragment();
            return true;
        }
        return super.onBackPressed();
    }

    private AwesomeFragment mRootFragment;

    public void setRootFragment(@NonNull AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("StackFragment is at added state，can not `setRootFragment` any more.");
        }
        mRootFragment = fragment;
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
        return mRootFragment;
    }

    public void pushFragment(@NonNull AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, () -> {
        }, TransitionAnimation.Push));
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, completion, TransitionAnimation.Push));
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, completion, animation));
    }

    protected void pushFragmentSync(AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, animation);
        completion.run();
    }

    public void popToFragment(@NonNull AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, () -> {
        }, TransitionAnimation.Push));
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, completion, TransitionAnimation.Push));
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, completion, animation));
    }

    protected void popToFragmentSync(AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null || topFragment == fragment) {
            completion.run();
            return;
        }

        topFragment.setAnimation(animation);
        fragment.setAnimation(animation);

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();
        fragmentManager.popBackStack(fragment.getSceneId(), 0);
        fragmentManager.beginTransaction().setMaxLifecycle(fragment, Lifecycle.State.RESUMED).commit();
        completion.run();

        handleFragmentResult(fragment, topFragment);
    }

    public void popFragment() {
        scheduleTaskAtStarted(() -> popFragmentSync(() -> {
        }, TransitionAnimation.Push));
    }

    public void popFragment(@NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popFragmentSync(completion, TransitionAnimation.Push));
    }

    public void popFragment(@NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> popFragmentSync(completion, animation));
    }

    protected void popFragmentSync(@NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            completion.run();
            return;
        }

        AwesomeFragment precursor = FragmentHelper.getFragmentBefore(topFragment);
        if (precursor == null) {
            completion.run();
            return;
        }

        popToFragmentSync(precursor, completion, animation);
    }

    public void popToRootFragment() {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(() -> {
        }, TransitionAnimation.Push));
    }

    public void popToRootFragment(@NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(completion, TransitionAnimation.Push));
    }

    public void popToRootFragment(@NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(completion, animation));
    }

    protected void popToRootFragmentSync(@NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        AwesomeFragment rootFragment = getRootFragment();
        if (rootFragment == null) {
            completion.run();
            return;
        }
        popToFragmentSync(rootFragment, completion, animation);
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, () -> {
        }, TransitionAnimation.Redirect, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, completion, TransitionAnimation.Redirect, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, completion, animation, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation, @NonNull AwesomeFragment from) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, completion, animation, from));
    }

    protected void redirectToFragmentSync(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation, @Nullable AwesomeFragment from) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            completion.run();
            return;
        }

        if (from == null) {
            from = topFragment;
        }

        AwesomeFragment precursor = FragmentHelper.getFragmentBefore(from);

        topFragment.setAnimation(animation);
        if (precursor != null && precursor.isAdded()) {
            precursor.setAnimation(animation);
        }

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();
        fragmentManager.popBackStack(from.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (precursor != null && precursor.isAdded()) {
            transaction.hide(precursor);
            transaction.setMaxLifecycle(precursor, Lifecycle.State.STARTED);
        }
        fragment.setAnimation(animation);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();

        completion.run();
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

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    private boolean mDragging = false;

    @Override
    public void onViewDragStateChanged(int state, float scrollPercent) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            return;
        }

        if (state == SwipeBackLayout.STATE_DRAGGING) {
            handleDraggingState(topFragment);
            return;
        }

        if (state == SwipeBackLayout.STATE_IDLE) {
            handleIdleState(scrollPercent, topFragment);
        }
    }

    private void handleDraggingState(AwesomeFragment topFragment) {
        mDragging = true;
        AwesomeFragment precursor = FragmentHelper.getFragmentBefore(topFragment);
        if (precursor == null) {
            return;
        }

        if (precursor.getView() != null) {
            precursor.getView().setVisibility(View.VISIBLE);
        }

        if (precursor != getRootFragment() || !shouldHideTabBarWhenPushed()) {
            return;
        }

        TabBarFragment tabBarFragment = getTabBarFragment();
        if (tabBarFragment == null) {
            return;
        }

        if (tabBarFragment.getView() == null) {
            return;
        }

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
        mSwipeBackLayout.setTabBar(bitmapDrawable);
    }

    private void handleIdleState(float scrollPercent, AwesomeFragment topFragment) {
        mSwipeBackLayout.setTabBar(null);
        mDragging = false;

        AwesomeFragment precursor = FragmentHelper.getFragmentBefore(topFragment);
        if (precursor == null) {
            return;
        }

        if (precursor.getView() != null) {
            precursor.getView().setVisibility(View.GONE);
        }

        if (scrollPercent >= 1.0f) {
            popFragmentSync(() -> {
            }, TransitionAnimation.None);
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.executePendingTransactions();
        }
    }

    public boolean shouldHideTabBarWhenPushed() {
        AwesomeFragment root = getRootFragment();
        if (root != null && root.isAdded()) {
            return root.hideTabBarWhenPushed();
        }
        return true;
    }

    @Override
    public boolean shouldSwipeBack() {
        AwesomeFragment top = getTopFragment();
        if (top == null) {
            return false;
        }
        return mStyle.isSwipeBackEnabled()
                && FragmentHelper.getBackStackEntryCount(this) > 1
                && top.isBackInteractive()
                && top.isSwipeBackEnabled();
    }

}
