package com.navigation.androidx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

/**
 * Created by Listen on 2018/1/11.
 */

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener {

    private static final String MIN_DRAWER_MARGIN_KEY = "MIN_DRAWER_MARGIN_KEY";
    private static final String MAX_DRAWER_WIDTH_KEY = "MAX_DRAWER_WIDTH_KEY";

    private DrawerLayout drawerLayout;
    private int minDrawerMargin = 64; // dp
    private int maxDrawerWidth; // dp

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nav_fragment_drawer, container, false);
        drawerLayout = root.findViewById(R.id.drawer);
        drawerLayout.addDrawerListener(this);

        if (savedInstanceState != null) {
            minDrawerMargin = savedInstanceState.getInt(MIN_DRAWER_MARGIN_KEY, 64);
            maxDrawerWidth = savedInstanceState.getInt(MAX_DRAWER_WIDTH_KEY);
        }

        FrameLayout menuLayout = drawerLayout.findViewById(R.id.drawer_menu);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menuLayout.getLayoutParams();
        int screenWidth = AppUtils.getScreenWidth(requireContext());
        int margin1 = AppUtils.dp2px(requireContext(), minDrawerMargin);
        if (margin1 > screenWidth) {
            margin1 = screenWidth;
        } else if (margin1 < 0) {
            margin1 = 0;
        }
        if (maxDrawerWidth <= 0 || maxDrawerWidth > screenWidth) {
            maxDrawerWidth = screenWidth;
        }
        int margin2 = screenWidth - AppUtils.dp2px(requireContext(), maxDrawerWidth);
        int margin = Math.max(margin1, margin2);
        layoutParams.rightMargin = margin - AppUtils.dp2px(requireContext(), 64);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            if (contentFragment == null) {
                throw new IllegalArgumentException("必须调用 `setContentFragment` 设置 contentFragment");
            }
            if (menuFragment == null) {
                throw new IllegalArgumentException("必须调用 `setMenuFragment` 设置 menuFragment");
            }

            FragmentHelper.addFragmentToAddedList(getChildFragmentManager(), R.id.drawer_content, contentFragment, Lifecycle.State.RESUMED);
            FragmentHelper.addFragmentToAddedList(getChildFragmentManager(), R.id.drawer_menu, menuFragment, Lifecycle.State.STARTED, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        opened = opening = isMenuOpened();
        closed = !isMenuOpened();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MIN_DRAWER_MARGIN_KEY, minDrawerMargin);
        outState.putInt(MAX_DRAWER_WIDTH_KEY, maxDrawerWidth);
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

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getContentFragment();
    }

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForNavigationBarAppearance() {
        return getContentFragment();
    }

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForStatusBarHidden() {
        if (opening || opened) {
            return null;
        }
        return getContentFragment();
    }

    @Override
    protected boolean preferredStatusBarHidden() {
        return shouldHideStatusBarWhenMenuOpened() || super.preferredStatusBarHidden();
    }

    protected boolean shouldHideStatusBarWhenMenuOpened() {
        return (opening || opened) && isStatusBarTranslucent() && !AppUtils.isCutout(requireActivity());
    }

    @Override
    protected boolean onBackPressed() {
        if (isMenuOpened()) {
            closeMenu();
            return true;
        }
        return super.onBackPressed();
    }

    private boolean closed = true;
    private boolean opened = false;
    private boolean closing;
    private boolean opening;

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        if (slideOffset != 0) {
            if (closed) {
                if (!opening) {
                    opening = true;
                    setNeedsStatusBarAppearanceUpdate(true);
                }
            } else if (opened) {
                if (!closing) {
                    closing = true;
                    setNeedsStatusBarAppearanceUpdate(true);
                }
            }
        }

        if (slideOffset == 0) {
            closed = true;
            opened = false;
            opening = false;
            setNeedsStatusBarAppearanceUpdate(true);
        } else if (slideOffset == 1) {
            opened = true;
            closed = false;
            closing = false;
        }
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        scheduleTaskAtStarted(() -> {
            AwesomeFragment menu = getMenuFragment();
            AwesomeFragment content = getContentFragment();
            if (menu != null && content != null) {
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .setPrimaryNavigationFragment(menu)
                        .setMaxLifecycle(menu, Lifecycle.State.RESUMED)
                        .commit();
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);

                View menuView = menu.getView();
                if (menuView != null) {
                    menuView.setClickable(true);
                }
            }
        });
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        scheduleTaskAtStarted(() -> {
            AwesomeFragment menu = getMenuFragment();
            AwesomeFragment content = getContentFragment();
            if (menu != null && content != null) {
                FragmentManager fragmentManager = getChildFragmentManager();
                fragmentManager.beginTransaction()
                        .setPrimaryNavigationFragment(content)
                        .setMaxLifecycle(menu, Lifecycle.State.STARTED)
                        .commit();
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);
            }
        });
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // Log.i(TAG, getDebugTag() + " drawer state:" + newState);
    }

    private AwesomeFragment contentFragment;

    public void setContentFragment(final AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("DrawerFragment 已处于 added 状态，不可以再设置 contentFragment");
        }
        contentFragment = fragment;
    }

    @Nullable
    public AwesomeFragment getContentFragment() {
        if (!isAdded()) {
            return null;
        }
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
    }

    private AwesomeFragment menuFragment;

    public void setMenuFragment(AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("DrawerFragment 已处于 added 状态，不可以再设置 menuFragment");
        }
        menuFragment = fragment;
    }

    @Nullable
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
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void closeMenu() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void setDrawerLockMode(final int lockMode) {
        scheduleTaskAtStarted(() -> {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(lockMode);
            }
        });
    }

    public void setMenuInteractive(boolean enabled) {
        setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
            return drawerLayout.isDrawerOpen(GravityCompat.START);
        }
        return false;
    }

    public boolean isMenuPrimary() {
        return !(closed || closing);
    }

}
