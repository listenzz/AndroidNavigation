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

/**
 * Created by Listen on 2018/1/11.
 */

public class StackFragment extends AwesomeFragment implements SwipeBackLayout.SwipeListener {

    private SwipeBackLayout mSwipeBackLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root;
        if (mStyle.isSwipeBackEnabled()) {
            root = inflater.inflate(R.layout.nav_fragment_navigation_swipe_back, container, false);
            mSwipeBackLayout = root.findViewById(R.id.navigation_content);
            mSwipeBackLayout.setSwipeListener(this);
            int scrimAlpha = mStyle.getScrimAlpha();
            mSwipeBackLayout.setScrimColor(scrimAlpha << 24);
        } else {
            root = inflater.inflate(R.layout.nav_fragment_navigation, container, false);
        }
        return root;
    }

    @Override
    protected void performCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.performCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState == null) {
            if (mRootFragment == null) {
                throw new IllegalArgumentException("Must specify rootFragment by `setRootFragment`.");
            } else {
                setRootFragmentSync(mRootFragment);
                mRootFragment = null;
            }
        }
    }

    private void setRootFragmentSync(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, TransitionAnimation.None);
    }

    @Override
    public boolean isParentAwesomeFragment() {
        return true;
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
            if (topFragment != null) {
                popFragment();
            }
            return true;
        } else {
            return super.onBackPressed();
        }
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
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, TransitionAnimation.Push, null));
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, animated ? TransitionAnimation.Push : TransitionAnimation.None, null));
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, TransitionAnimation.Push, completion));
    }

    public void pushFragment(@NonNull AwesomeFragment fragment, boolean animated, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> pushFragmentSync(fragment, animated ? TransitionAnimation.Push : TransitionAnimation.None, completion));
    }

    protected void pushFragmentSync(AwesomeFragment fragment, @NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, animation);
        if (completion != null) {
            completion.run();
        }
    }

    public void popToFragment(@NonNull AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, TransitionAnimation.Push, null));
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, animated ? TransitionAnimation.Push : TransitionAnimation.None, null));
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, TransitionAnimation.Push, completion));
    }

    public void popToFragment(@NonNull AwesomeFragment fragment, boolean animated, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToFragmentSync(fragment, animated ? TransitionAnimation.Push : TransitionAnimation.None, completion));
    }

    protected void popToFragmentSync(AwesomeFragment fragment, @NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null || topFragment == fragment) {
            if (completion != null) {
                completion.run();
            }
            return;
        }
        topFragment.setAnimation(animation);
        fragment.setAnimation(animation);

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();
        fragmentManager.popBackStack(fragment.getSceneId(), 0);
        fragmentManager.beginTransaction().setMaxLifecycle(fragment, Lifecycle.State.RESUMED).commit();

        handleFragmentResult(fragment, topFragment);

        if (completion != null) {
            completion.run();
        }
    }

    public void popFragment() {
        scheduleTaskAtStarted(() -> popFragmentSync(TransitionAnimation.Push, null));
    }

    public void popFragment(boolean animated) {
        scheduleTaskAtStarted(() -> popFragmentSync(animated ? TransitionAnimation.Push : TransitionAnimation.None, null));
    }

    public void popFragment(@NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popFragmentSync(TransitionAnimation.Push, completion));
    }

    public void popFragment(boolean animated, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popFragmentSync(animated ? TransitionAnimation.Push : TransitionAnimation.None, completion));
    }

    protected void popFragmentSync(@NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        AwesomeFragment previous = FragmentHelper.getFragmentBefore(topFragment);
        if (previous != null) {
            popToFragmentSync(previous, animation, null);
        }

        if (completion != null) {
            completion.run();
        }
    }

    public void popToRootFragment() {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(TransitionAnimation.Push, null));
    }

    public void popToRootFragment(boolean animated) {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(animated ? TransitionAnimation.Push : TransitionAnimation.None, null));
    }

    public void popToRootFragment(@NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(TransitionAnimation.Push, completion));
    }

    public void popToRootFragment(boolean animated, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> popToRootFragmentSync(animated ? TransitionAnimation.Push : TransitionAnimation.None, completion));
    }

    protected void popToRootFragmentSync(@NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        AwesomeFragment rootFragment = getRootFragment();
        if (rootFragment != null) {
            popToFragmentSync(rootFragment, animation, null);
        }
        if (completion != null) {
            completion.run();
        }
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, TransitionAnimation.Redirect, null, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, boolean animated) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, animated ? TransitionAnimation.Redirect : TransitionAnimation.None, null, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, TransitionAnimation.Redirect, completion, null));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, @NonNull AwesomeFragment from) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, TransitionAnimation.Redirect, null, from));
    }

    public void redirectToFragment(@NonNull AwesomeFragment fragment, boolean animated, @Nullable Runnable completion, @Nullable AwesomeFragment from) {
        scheduleTaskAtStarted(() -> redirectToFragmentSync(fragment, animated ? TransitionAnimation.Redirect : TransitionAnimation.None, completion, from));
    }

    protected void redirectToFragmentSync(@NonNull AwesomeFragment fragment, @NonNull TransitionAnimation animation, @Nullable Runnable completion, @Nullable AwesomeFragment from) {
        FragmentManager fragmentManager = getChildFragmentManager();

        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == null) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        if (from == null) {
            from = topFragment;
        }

        AwesomeFragment previous = FragmentHelper.getFragmentBefore(from);

        topFragment.setAnimation(animation);
        if (previous != null && previous.isAdded()) {
            previous.setAnimation(animation);
        }

        fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();

        fragmentManager.popBackStack(from.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (previous != null && previous.isAdded()) {
            transaction.hide(previous);
            transaction.setMaxLifecycle(previous, Lifecycle.State.STARTED);
        }
        fragment.setAnimation(animation);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();

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
    public StackFragment getStackFragment() {
        StackFragment navF = super.getStackFragment();
        if (navF != null) {
            AwesomeFragment parent = navF.getParentAwesomeFragment();
            while (parent != null) {
                if (parent instanceof StackFragment && parent.getWindow() == navF.getWindow()) {
                    throw new IllegalStateException("should not nest StackFragment in the same window.");
                }
                parent = parent.getParentAwesomeFragment();
            }
        }
        return navF;
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
            mDragging = true;
            AwesomeFragment previous = FragmentHelper.getFragmentBefore(topFragment);

            if (previous != null && previous.getView() != null) {
                previous.getView().setVisibility(View.VISIBLE);
            }

            if (previous != null && previous == getRootFragment() && previous.isAdded() && shouldHideTabBarWhenPushed()) {
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
                    mSwipeBackLayout.setTabBar(bitmapDrawable);
                }
            }

        } else if (state == SwipeBackLayout.STATE_IDLE) {
            AwesomeFragment previous = FragmentHelper.getFragmentBefore(topFragment);

            if (previous != null && previous.getView() != null) {
                previous.getView().setVisibility(View.GONE);
            }

            if (previous != null && scrollPercent >= 1.0f) {
                popFragmentSync(TransitionAnimation.None, null);
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.executePendingTransactions();
            }

            mSwipeBackLayout.setTabBar(null);
            mDragging = false;
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
