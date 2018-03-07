package me.listenzz.navigation;

import android.os.Bundle;
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

public class NavigationFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_fragment_navigation, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        AwesomeFragment top = getTopFragment();
        if (top != null) {
            setUserVisibleHint(!hidden);
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
            if (topFragment.backInteractive() || !topFragment.shouldHideBackButton()) {
                popFragment();
            }
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    public void setRootFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                addRootFragment(fragment);
            }
        });
    }

    private void addRootFragment(AwesomeFragment fragment) {
        AwesomeFragment root =  getRootFragment();
        if (root != null) {
            throw new IllegalStateException("不可以重复设置 rootFragment");
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void pushFragment(AwesomeFragment fragment) {
        int count = getChildFragmentCountAtBackStack();
        if (count == 0) {
            throw new IllegalStateException("请先调用 #setRootFragment 添加 rootFragment.");
        }
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.Push);
    }

    public void popToFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executePopToFragment(fragment);
            }
        });
    }

    private void executePopToFragment(AwesomeFragment fragment) {
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

    public void replaceFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeReplaceFragment(fragment);
            }
        });
    }

    private void executeReplaceFragment(AwesomeFragment fragment) {
        int count = getChildFragmentCountAtBackStack();
        if (count == 0) {
            throw new IllegalStateException("请先调用 #setRootFragment 添加 rootFragment.");
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(fragmentManager, topFragment);
        topFragment.setAnimation(PresentAnimation.Fade);

        if (aheadFragment != null) {
            aheadFragment.setAnimation(PresentAnimation.Fade);
        }

        fragmentManager.popBackStack();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());

        if (aheadFragment != null) {
            transaction.hide(aheadFragment);
        }
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void replaceToRootFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeReplaceRootFragment(fragment);
            }
        });
    }

    private void executeReplaceRootFragment(AwesomeFragment fragment) {
        AwesomeFragment rootFragment = getRootFragment();
        if (rootFragment == null) {
            throw new IllegalStateException("请先调用 #setRootFragment 添加 rootFragment.");
        }

        AwesomeFragment topFragment = getTopFragment();
        topFragment.setAnimation(PresentAnimation.Fade);
        rootFragment.setAnimation(PresentAnimation.Fade);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.popBackStack(rootFragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void setFragments(List<AwesomeFragment> fragments) {

        // TODO
        // 弹出所有旧的 fragment

        // 添加所有新的 fragment
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

    public AwesomeFragment getTopFragment() {
        if (isAdded()) {
            return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.navigation_content);
        } else {
            return null;
        }
    }

}
