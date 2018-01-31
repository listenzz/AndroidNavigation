package com.navigation.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener {

    private DrawerLayout drawerLayout;

    private AwesomeFragment contentFragment;
    private AwesomeFragment menuFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        if (savedInstanceState == null && contentFragment != null && menuFragment != null) {
            addFragment(R.id.drawer_menu, menuFragment, PresentAnimation.None);
            addFragment(R.id.drawer_content, contentFragment, PresentAnimation.None);
        } else {
            contentFragment = (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
            menuFragment = (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_menu);
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
    protected boolean preferredStatusBarHidden() {
        return isMenuOpened() || super.preferredStatusBarHidden();
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
    public boolean isContainer() {
        return true;
    }

    @Override
    public void addFragment(int containerId, AwesomeFragment fragment, PresentAnimation animation) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, fragment.getSceneId());
        transaction.setPrimaryNavigationFragment(fragment);
        transaction.commit();
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
        if (fragment instanceof NavigationFragment) {
            return (NavigationFragment) fragment;
        }
        AwesomeFragment primary = (AwesomeFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return findClosestNavigationFragment(primary);
        }
        List<AwesomeFragment> children = fragment.getAddedChildFragments();
        if (children != null && children.size() > 0) {
            return findClosestNavigationFragment(children.get(children.size() - 1));
        }
        return null;
    }

    @Override
    public TabBarFragment getTabBarFragment() {
        TabBarFragment tabBarFragment = super.getTabBarFragment();
        if (tabBarFragment == null && getContentFragment() != null) {
            return findClosestTabBarFragment(getContentFragment());
        }
        return null;
    }

    private TabBarFragment findClosestTabBarFragment(AwesomeFragment fragment) {
        if (fragment instanceof TabBarFragment) {
            return (TabBarFragment) fragment;
        }
        AwesomeFragment primary = (AwesomeFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return findClosestTabBarFragment(primary);
        }
        List<AwesomeFragment> children = fragment.getAddedChildFragments();
        if (children != null && children.size() > 0) {
            return findClosestTabBarFragment(children.get(children.size() - 1));
        }
        return null;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        setStatusBarHidden(slideOffset != 0 || super.preferredStatusBarHidden());
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getMenuFragment()).commit();
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getContentFragment()).commit();
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public void setContentFragment(AwesomeFragment fragment) {
        this.contentFragment = fragment;
        if (isAtLeastCreated()) {
            addFragment(R.id.drawer_content, contentFragment, PresentAnimation.None);
        }
    }

    public AwesomeFragment getContentFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
    }

    public void setMenuFragment(AwesomeFragment fragment) {
        this.menuFragment = fragment;
        if (isAtLeastCreated()) {
            addFragment(R.id.drawer_menu, menuFragment, PresentAnimation.None);
            AwesomeFragment content = getContentFragment();
            if (content != null) {
                getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(content).commit();
            }
        }
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
