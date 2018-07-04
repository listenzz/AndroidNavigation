package me.listenzz.navigation;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                pushFragmentInternal(fragment);
            }
        });
    }

    private void pushFragmentInternal(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.Push);
    }

    public void popToFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                popToFragmentInternal(fragment);
            }
        });
    }

    private void popToFragmentInternal(AwesomeFragment fragment) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == fragment) {
            return;
        }
        topFragment.setAnimation(PresentAnimation.Push);
        fragment.setAnimation(PresentAnimation.Push);

        fragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());
        getChildFragmentManager().popBackStack(fragment.getSceneId(), 0);
    }

    public void popFragment() {
        AwesomeFragment after = FragmentHelper.getLatterFragment(getChildFragmentManager(), getTopFragment());
        if (after != null) {
            popToFragment(this);
            return;
        }

        AwesomeFragment before = FragmentHelper.getAheadFragment(getChildFragmentManager(), getTopFragment());
        if (before != null) {
            popToFragment(before);
        }
    }

    public void popToRootFragment() {
        AwesomeFragment awesomeFragment = getRootFragment();
        if (awesomeFragment != null) {
            popToFragment(getRootFragment());
        }
    }

    public void replaceFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    replaceFragmentInternal(fragment, PresentAnimation.Fade);
                } else {
                    replaceFragmentInternal(fragment, PresentAnimation.None);
                }
            }
        });
    }

    private void replaceFragmentInternal(AwesomeFragment fragment, PresentAnimation animation) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.executePendingTransactions();

        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(fragmentManager, topFragment);
        topFragment.setAnimation(animation);
        if (aheadFragment != null) {
            aheadFragment.setAnimation(animation);
        }
        if (animation == PresentAnimation.None) {
            fragmentManager.popBackStackImmediate();
        } else {
            fragmentManager.popBackStack();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (aheadFragment != null) {
            transaction.hide(aheadFragment);
        }
        fragment.setAnimation(animation);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
        if (animation == PresentAnimation.None) {
            fragmentManager.executePendingTransactions();
        }
    }

    public void replaceToRootFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    replaceRootFragmentInternal(fragment, PresentAnimation.Fade);
                } else {
                    replaceRootFragmentInternal(fragment, PresentAnimation.None);
                }
            }
        });
    }

    private void replaceRootFragmentInternal(AwesomeFragment fragment, PresentAnimation animation) {
        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment rootFragment = getRootFragment();
        topFragment.setAnimation(animation);
        rootFragment.setAnimation(animation);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.executePendingTransactions();

        if (animation == PresentAnimation.None) {
            fragmentManager.popBackStackImmediate(rootFragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            fragmentManager.popBackStack(rootFragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragment.setAnimation(animation);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
        if (animation == PresentAnimation.None) {
            fragmentManager.executePendingTransactions();
        }
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
            if (parent != null) {
                NavigationFragment parentNavF = parent.getNavigationFragment();
                if (parentNavF != null && parentNavF.getWindow() == navF.getWindow()) {
                    throw new IllegalStateException("should not nest NavigationFragment in the same window.");
                }
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
            }
            if (aheadFragment != null && scrollPercent >= 1.0f) {
                topFragment.setAnimation(PresentAnimation.None);
                aheadFragment.setAnimation(PresentAnimation.None);
                aheadFragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());
                getChildFragmentManager().popBackStackImmediate(aheadFragment.getSceneId(), 0);
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
}
