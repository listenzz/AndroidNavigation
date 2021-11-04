package com.navigation.androidx;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Created by Listen on 2018/06/29.
 */
public class DefaultTabBarProvider implements TabBarProvider {

    private TabBar tabBar;

    private TabBarFragment tabBarFragment;

    @Override
    public View onCreateTabBar(@NonNull List<TabBarItem> tabBarItems, @NonNull TabBarFragment tabBarFragment, @Nullable Bundle savedInstanceState) {
        TabBar tabBar = new TabBar(tabBarFragment.requireContext());
        this.tabBarFragment = tabBarFragment;
        this.tabBar = tabBar;
        initialise(tabBarItems);
        return tabBar;
    }

    @Override
    public void onDestroyTabBar() {
        tabBar.setTabSelectedListener(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }

    @Override
    public void setSelectedIndex(int index) {
        if (tabBar != null) {
            tabBar.selectTab(index, false);
        }
    }

    private void initialise(List<TabBarItem> tabBarItems) {
        Style style = tabBarFragment.style;
        tabBar.setBarBackgroundColor(style.getTabBarBackgroundColor());
        tabBar.setBadgeColor(style.getTabBarBadgeColor());

        if (style.getTabBarItemColor() != null) {
            tabBar.setSelectedItemColor(style.getTabBarItemColor());
            if (style.getTabBarUnselectedItemColor() != null) {
                tabBar.setUnselectedItemColor(style.getTabBarUnselectedItemColor());
            }
        }

        tabBar.setShadowDrawable(style.getTabBarShadow());
        tabBar.setTabSelectedListener(new TabBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                tabBarFragment.setSelectedIndex(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                tabBarFragment.setSelectedIndex(position);
            }
        });

        for (int i = 0, size = tabBarItems.size(); i < size; i++) {
            TabBarItem tabBarItem = tabBarItems.get(i);
            tabBar.addTabBarItem(tabBarItem);
        }

        tabBar.initialise(tabBarFragment.getSelectedIndex());
    }

    @Override
    public void updateTabBar(@NonNull Bundle options) {

    }
}
