package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnavigation.R;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener {

    DrawerLayout drawerLayout;

    AwesomeFragment contentFragment;
    AwesomeFragment menuFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drawer, container, false);
        drawerLayout = root.findViewById(R.id.drawer);
        drawerLayout.addDrawerListener(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        drawerLayout.removeDrawerListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            addFragment(R.id.drawer_menu, menuFragment, PresentAnimation.None);
            addFragment(R.id.drawer_content, contentFragment, PresentAnimation.None);
        }
    }

    @Override
    protected AwesomeFragment childFragmentForStatusBarColor() {
        return getContentFragment();
    }

    @Override
    protected AwesomeFragment childFragmentForStatusBarStyle() {
        return getContentFragment();
    }

    @Override
    protected AwesomeFragment childFragmentForStatusBarHidden() {
        return getContentFragment();
    }

    @Override
    protected boolean prefersStatusBarHidden() {
        return isMenuOpened() || super.prefersStatusBarHidden();
    }

    @Override
    protected boolean onBackPressed() {
        if (isMenuOpened()) {
            closeMenu();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public NavigationFragment getNavigationFragment() {
        NavigationFragment navigationFragment = super.getNavigationFragment();
        if (navigationFragment == null && getContentFragment() != null) {
            return findClosestNavigationFragment(getContentFragment());
        }
        return null;
    }

    private NavigationFragment findClosestNavigationFragment(AwesomeFragment fragment) {
        if (fragment instanceof  NavigationFragment) {
            return (NavigationFragment) fragment;
        }
        AwesomeFragment primary = (AwesomeFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return findClosestNavigationFragment(primary);
        }
        List<AwesomeFragment> children =  fragment.getFragments();
        if (children != null && children.size() > 0) {
            return findClosestNavigationFragment(children.get(children.size() -1));
        }
        return null;
    }

    @Override
    public TabBarFragment getTabBarFragment() {
        TabBarFragment navigationFragment = super.getTabBarFragment();
        if (navigationFragment == null && getContentFragment() != null) {
            AwesomeFragment innermost =  getContentFragment().getInnermostFragment();
            if (innermost != null) {
                return innermost.getTabBarFragment();
            }
        }
        return null;
    }

    private TabBarFragment findClosestTabBarFragment(AwesomeFragment fragment) {
        if (fragment instanceof  TabBarFragment) {
            return (TabBarFragment) fragment;
        }
        AwesomeFragment primary = (AwesomeFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return findClosestTabBarFragment(primary);
        }
        List<AwesomeFragment> children =  fragment.getFragments();
        if (children != null && children.size() > 0) {
            return findClosestTabBarFragment(children.get(children.size() -1));
        }
        return null;
    }



    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getMenuFragment()).commit();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getContentFragment()).commit();
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public void setContentFragment(AwesomeFragment fragment) {
        this.contentFragment = fragment;

    }

    public AwesomeFragment getContentFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
    }

    public void setMenuFragment(AwesomeFragment fragment) {
        this.menuFragment = fragment;

    }

    public AwesomeFragment getMenuFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_menu);
    }

    public void openMenu() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(Gravity.START);
            setStatusBarHidden(true);
        }
    }

    public void closeMenu() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(Gravity.START);
            setStatusBarHidden(false);
        }
    }

    public void toggleMenu() {
        if (isMenuOpened()) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    public boolean isMenuOpened() {
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(Gravity.START);
        }
        return false;
    }

}
