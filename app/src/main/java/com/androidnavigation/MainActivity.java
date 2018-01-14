package com.androidnavigation;

import android.os.Bundle;

import com.androidnavigation.fragment.AwesomeActivity;
import com.androidnavigation.fragment.DrawerFragment;
import com.androidnavigation.fragment.NavigationFragment;
import com.androidnavigation.fragment.TabBarFragment;
import com.androidnavigation.fragment.TabBarItem;

public class MainActivity extends AwesomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // AndroidBug5497Workaround.assistActivity(findViewById(android.R.id.content));

        if (savedInstanceState == null) {

            TestFragment testFragment = new TestFragment();
            NavigationFragment navigation = new NavigationFragment();
            navigation.setRootFragment(testFragment);
            navigation.setTabBarItem(new TabBarItem(R.drawable.ic_home_white_24dp, "Home"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            statusBar.setTabBarItem(new TabBarItem(R.drawable.ic_discover_white_24dp, "Status"));

            TabBarFragment tabBarFragment = new TabBarFragment();
            tabBarFragment.setFragments(navigation, statusBar);

            DrawerFragment drawerFragment = new DrawerFragment();
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMenuFragment(new FirstFragment());

            setRootFragment(drawerFragment);
        }
    }


}
