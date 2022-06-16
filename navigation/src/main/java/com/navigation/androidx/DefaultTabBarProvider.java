package com.navigation.androidx;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DefaultTabBarProvider implements TabBarProvider {

    private TabBar mTabBar;

    private TabBarFragment mTabBarFragment;

    @Override
    public View onCreateTabBar(@NonNull List<TabBarItem> tabBarItems, @NonNull TabBarFragment tabBarFragment, @Nullable Bundle savedInstanceState) {
        TabBar tabBar = new TabBar(tabBarFragment.requireContext());
        mTabBarFragment = tabBarFragment;
        mTabBar = tabBar;
        initialise(tabBarItems);
        return tabBar;
    }

    @Override
    public void onDestroyTabBar() {
        mTabBar.setTabSelectedListener(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }

    @Override
    public void setSelectedIndex(int index) {
        if (mTabBar != null) {
            mTabBar.selectTab(index, false);
        }
    }

    private void initialise(List<TabBarItem> tabBarItems) {
        Style style = mTabBarFragment.mStyle;
        mTabBar.setBarBackgroundColor(style.getTabBarBackgroundColor());
        mTabBar.setBadgeColor(style.getTabBarBadgeColor());

        if (style.getTabBarItemColor() != null) {
            mTabBar.setSelectedItemColor(style.getTabBarItemColor());
            if (style.getTabBarUnselectedItemColor() != null) {
                mTabBar.setUnselectedItemColor(style.getTabBarUnselectedItemColor());
            }
        }

        mTabBar.setShadowDrawable(style.getTabBarShadow());
        mTabBar.setTabSelectedListener(new TabBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                mTabBarFragment.setSelectedIndex(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                mTabBarFragment.setSelectedIndex(position);
            }
        });

        for (int i = 0, size = tabBarItems.size(); i < size; i++) {
            TabBarItem tabBarItem = tabBarItems.get(i);
            mTabBar.addTabBarItem(tabBarItem);
        }

        mTabBar.initialise(mTabBarFragment.getSelectedIndex());
    }

    @Override
    public void updateTabBar(@NonNull Bundle options) {

    }
}
