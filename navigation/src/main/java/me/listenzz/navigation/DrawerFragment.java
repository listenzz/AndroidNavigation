package me.listenzz.navigation;

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
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener {

    private DrawerLayout drawerLayout;
    private boolean isClosing;
    private int minDrawerMargin = 64; // dp
    private int maxDrawerWidth; // dp

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nav_fragment_drawer, container, false);
        drawerLayout = root.findViewById(R.id.drawer);
        drawerLayout.addDrawerListener(this);

        FrameLayout menuLayout = drawerLayout.findViewById(R.id.drawer_menu);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menuLayout.getLayoutParams();
        int screenWidth = AppUtils.getScreenWidth(getContext());
        int margin1 = AppUtils.dp2px(getContext(), minDrawerMargin);
        if (margin1 > screenWidth) {
            margin1 = screenWidth;
        } else if (margin1 < 0) {
            margin1 = 0;
        }
        if (maxDrawerWidth <= 0 || maxDrawerWidth > screenWidth) {
            maxDrawerWidth = screenWidth;
        }
        int margin2 = screenWidth - AppUtils.dp2px(getContext(), maxDrawerWidth);
        int margin = Math.max(margin1, margin2);
        layoutParams.rightMargin = margin - AppUtils.dp2px(getContext(), 64);

        return root;
    }

    @Override
    public void onDestroyView() {
        drawerLayout.removeDrawerListener(this);
        super.onDestroyView();
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getContentFragment();
    }

    @Override
    protected boolean preferredStatusBarHidden() {
        if (isStatusBarTranslucent()) {
            return isMenuOpened() || super.preferredStatusBarHidden();
        } else {
            return super.preferredStatusBarHidden();
        }
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
        if (fragment instanceof NavigationFragment) {
            return (NavigationFragment) fragment;
        }
        AwesomeFragment primary = (AwesomeFragment) fragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary != null) {
            return findClosestNavigationFragment(primary);
        }
        List<AwesomeFragment> children = fragment.getChildFragmentsAtAddedList();
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
        List<AwesomeFragment> children = fragment.getChildFragmentsAtAddedList();
        if (children != null && children.size() > 0) {
            return findClosestTabBarFragment(children.get(children.size() - 1));
        }
        return null;
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        if (isStatusBarTranslucent()) {
            if (!isClosing) {
                setStatusBarHidden(slideOffset != 0 || super.preferredStatusBarHidden());
            }
            // Log.i(TAG, getDebugTag() + " onDrawerSlide");
        }
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getMenuFragment()).commit();
        getMenuFragment().setUserVisibleHint(true);
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        setNeedsStatusBarAppearanceUpdate();
        getChildFragmentManager().beginTransaction().setPrimaryNavigationFragment(getContentFragment()).commit();
        getMenuFragment().setUserVisibleHint(false);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // Log.i(TAG, getDebugTag() + " drawer state:" + newState);
    }

    public void setContentFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.drawer_content, fragment, fragment.getSceneId());
                transaction.setPrimaryNavigationFragment(fragment); // primary
                transaction.commit();
            }
        });
    }

    public AwesomeFragment getContentFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
    }

    public void setMenuFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                fragment.setUserVisibleHint(false);
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.drawer_menu, fragment, fragment.getSceneId());
                transaction.commit();
            }
        });
    }

    public AwesomeFragment getMenuFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_menu);
    }

    public void setMinDrawerMargin(int dp) {
        this.minDrawerMargin = dp;
    }

    public void setMaxDrawerWidth(int dp) {
        this.maxDrawerWidth = dp;
    }

    public void openMenu() {
        if (drawerLayout != null) {
            isClosing = false;
            drawerLayout.openDrawer(Gravity.START);
            if (isStatusBarTranslucent()) {
                setStatusBarHidden(true);
            }
        }
    }

    public void closeMenu() {
        if (drawerLayout != null) {
            isClosing = true;
            drawerLayout.closeDrawer(Gravity.START);
        }
    }

    public void setDrawerLockMode(int lockMode) {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(lockMode);
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
