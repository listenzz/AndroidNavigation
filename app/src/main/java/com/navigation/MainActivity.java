package com.navigation;

import android.os.Bundle;
import android.view.Gravity;

import com.navigation.library.AwesomeActivity;
import com.navigation.library.DrawerFragment;
import com.navigation.library.NavigationFragment;
import com.navigation.library.Style;
import com.navigation.library.TabBarFragment;
import com.navigation.library.TabBarItem;
import com.navigation.statusbar.TestStatusBarFragment;

public class MainActivity extends AwesomeActivity {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开启沉浸式
        setContentUnderStatusBar(true);

        if (savedInstanceState == null) {

            TestNavigationFragment testNavigationFragment = new TestNavigationFragment();
            NavigationFragment navigation = new NavigationFragment();
            navigation.setRootFragment(testNavigationFragment);
            String iconUri = "font://FontAwesome/" + fromCharCode(61732) + "/24";
            navigation.setTabBarItem(new TabBarItem(iconUri, "Navigation"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            statusBar.setTabBarItem(new TabBarItem(R.drawable.flower, "Status"));

            TabBarFragment tabBarFragment = new TabBarFragment();
            tabBarFragment.setFragments(navigation, statusBar);

            DrawerFragment drawerFragment = new DrawerFragment();
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMenuFragment(new MenuFragment());

            setRootFragment(drawerFragment);
        }
    }

    @Override
    protected void onCustomStyle(Style style) {
        style.setTitleGravity(Gravity.CENTER);
        // style.setBottomBarBackgroundColor("#3F51B5");
    }
}
