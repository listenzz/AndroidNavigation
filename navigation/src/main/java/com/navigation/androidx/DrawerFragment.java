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

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener {

    private static final String MIN_DRAWER_MARGIN_KEY = "MIN_DRAWER_MARGIN_KEY";
    private static final String MAX_DRAWER_WIDTH_KEY = "MAX_DRAWER_WIDTH_KEY";

    private DrawerLayout mDrawerLayout;
    private int mMinDrawerMargin = 64; // dp
    private int mMaxDrawerWidth; // dp

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nav_fragment_drawer, container, false);
        mDrawerLayout = root.findViewById(R.id.drawer);
        mDrawerLayout.addDrawerListener(this);

        if (savedInstanceState != null) {
            mMinDrawerMargin = savedInstanceState.getInt(MIN_DRAWER_MARGIN_KEY, 64);
            mMaxDrawerWidth = savedInstanceState.getInt(MAX_DRAWER_WIDTH_KEY);
        }

        FrameLayout menuLayout = mDrawerLayout.findViewById(R.id.drawer_menu);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menuLayout.getLayoutParams();
        int screenWidth = AppUtils.getScreenWidth(requireContext());
        int margin1 = AppUtils.dp2px(requireContext(), mMinDrawerMargin);
        if (margin1 > screenWidth) {
            margin1 = screenWidth;
        } else if (margin1 < 0) {
            margin1 = 0;
        }
        if (mMaxDrawerWidth <= 0 || mMaxDrawerWidth > screenWidth) {
            mMaxDrawerWidth = screenWidth;
        }
        int margin2 = screenWidth - AppUtils.dp2px(requireContext(), mMaxDrawerWidth);
        int margin = Math.max(margin1, margin2);
        layoutParams.rightMargin = margin - AppUtils.dp2px(requireContext(), 64);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            return;
        }

        if (mContentFragment == null) {
            throw new IllegalArgumentException("必须调用 `setContentFragment` 设置 contentFragment");
        }
        if (mMenuFragment == null) {
            throw new IllegalArgumentException("必须调用 `setMenuFragment` 设置 menuFragment");
        }

        FragmentHelper.addFragment(getChildFragmentManager(), R.id.drawer_content, mContentFragment, Lifecycle.State.RESUMED);
        FragmentHelper.addFragment(getChildFragmentManager(), R.id.drawer_menu, mMenuFragment, Lifecycle.State.STARTED, false);
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
        outState.putInt(MIN_DRAWER_MARGIN_KEY, mMinDrawerMargin);
        outState.putInt(MAX_DRAWER_WIDTH_KEY, mMaxDrawerWidth);
    }

    @Override
    public void onDestroyView() {
        mDrawerLayout.removeDrawerListener(this);
        super.onDestroyView();
    }

    @Override
    public boolean isLeafAwesomeFragment() {
        return false;
    }

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getContentFragment();
    }

    @Override
    protected boolean preferredStatusBarHidden() {
        return shouldHideStatusBarWhenMenuOpened() || super.preferredStatusBarHidden();
    }

    protected boolean shouldHideStatusBarWhenMenuOpened() {
        return (opening || opened) && requireView().isAttachedToWindow() && !SystemUI.isCutout(getWindow());
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
                    setNeedsStatusBarAppearanceUpdate();
                }
            } else if (opened) {
                if (!closing) {
                    closing = true;
                    setNeedsStatusBarAppearanceUpdate();
                }
            }
        }

        if (slideOffset == 0) {
            closed = true;
            opened = false;
            opening = false;
            setNeedsStatusBarAppearanceUpdate();
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

            if (menu == null || content == null) {
                return;
            }

            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(menu)
                    .setMaxLifecycle(menu, Lifecycle.State.RESUMED)
                    .commit();

            View menuView = menu.requireView();
            menuView.setClickable(true);
        });
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        scheduleTaskAtStarted(() -> {
            AwesomeFragment menu = getMenuFragment();
            AwesomeFragment content = getContentFragment();
            if (menu == null || content == null) {
                return;
            }

            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.beginTransaction()
                    .setPrimaryNavigationFragment(content)
                    .setMaxLifecycle(menu, Lifecycle.State.STARTED)
                    .commit();
        });
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // Log.i(TAG, getDebugTag() + " drawer state:" + newState);
    }

    private AwesomeFragment mContentFragment;

    public void setContentFragment(final AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("DrawerFragment 已处于 added 状态，不可以再设置 contentFragment");
        }
        mContentFragment = fragment;
    }

    @Nullable
    public AwesomeFragment getContentFragment() {
        if (isAdded()) {
            return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
        }
        return null;
    }

    @NonNull
    public AwesomeFragment requireContentFragment() {
        AwesomeFragment fragment = getContentFragment();
        if (fragment == null) {
            throw new NullPointerException("No content fragment");
        }
        return fragment;
    }

    private AwesomeFragment mMenuFragment;

    public void setMenuFragment(AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("DrawerFragment 已处于 added 状态，不可以再设置 menuFragment");
        }
        mMenuFragment = fragment;
    }

    @Nullable
    public AwesomeFragment getMenuFragment() {
        if (isAdded()) {
            return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_menu);
        }
        return null;
    }

    public void setMinDrawerMargin(int dp) {
        this.mMinDrawerMargin = dp;
    }

    public void setMaxDrawerWidth(int dp) {
        this.mMaxDrawerWidth = dp;
    }

    public void openMenu() {
        openMenu(() -> {
        });
    }

    public void openMenu(@NonNull Runnable completion) {
        if (isMenuOpened()) {
            completion.run();
            return;
        }

        if (mDrawerLayout == null) {
            throw new IllegalStateException("No drawer");
        }

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerLayout.removeDrawerListener(this);
                completion.run();
            }
        });
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeMenu() {
        closeMenu(() -> {
        });
    }

    public void closeMenu(@NonNull Runnable completion) {
        if (!isMenuOpened()) {
            completion.run();
            return;
        }

        if (mDrawerLayout == null) {
            throw new IllegalStateException("No drawer");
        }

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.removeDrawerListener(this);
                completion.run();
            }
        });
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setDrawerLockMode(final int lockMode) {
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(lockMode);
        }
    }

    public void setMenuInteractive(boolean enabled) {
        scheduleTaskAtStarted(() -> setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED));
    }

    public void toggleMenu() {
        toggleMenu(() -> {
        });
    }

    public void toggleMenu(@NonNull Runnable completion) {
        if (isMenuOpened()) {
            closeMenu(completion);
        } else {
            openMenu(completion);
        }
    }

    public boolean isMenuOpened() {
        if (mDrawerLayout != null) {
            return mDrawerLayout.isDrawerOpen(GravityCompat.START);
        }
        return false;
    }

    public boolean isMenuPrimary() {
        return !(closed || closing);
    }

}
