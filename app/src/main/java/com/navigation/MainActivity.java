package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;

import com.navigation.fragment.AwesomeActivity;
import com.navigation.fragment.DrawerFragment;
import com.navigation.fragment.Style;
import com.navigation.fragment.NavigationFragment;
import com.navigation.fragment.TabBarFragment;
import com.navigation.fragment.TabBarItem;

public class MainActivity extends AwesomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // AndroidBug5497Workaround.assistActivity(findViewById(android.R.id.content));

        if (savedInstanceState == null) {

            TestFragment testFragment = new TestFragment();
            NavigationFragment navigation = new NavigationFragment();
            navigation.setRootFragment(testFragment);
            navigation.setTabBarItem(new TabBarItem("ic_home_white_24dp", "Home"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            statusBar.setTabBarItem(new TabBarItem("ic_discover_white_24dp", "Status"));

            TabBarFragment tabBarFragment = new TabBarFragment();
            tabBarFragment.setFragments(navigation, statusBar);

            DrawerFragment drawerFragment = new DrawerFragment();
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMenuFragment(new FirstFragment());

            setRootFragment(drawerFragment);
        }
    }

    @Override
    protected void onCustomStyle(Style style) {
        style.setStatusBarColor(Color.parseColor("#303F9F"));
        style.setToolBarBackgroundColor(Color.parseColor("#3F51B5"));
        style.setTitleGravity(Gravity.CENTER);
        // style.setBottomBarBackgroundColor("#3F51B5");
    }
}
