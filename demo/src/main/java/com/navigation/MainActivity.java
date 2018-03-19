package com.navigation;

import android.os.Bundle;

import com.navigation.statusbar.TestStatusBarFragment;

import me.listenzz.navigation.AwesomeActivity;
import me.listenzz.navigation.DrawerFragment;
import me.listenzz.navigation.NavigationFragment;
import me.listenzz.navigation.Style;
import me.listenzz.navigation.TabBarFragment;
import me.listenzz.navigation.TabBarItem;

public class MainActivity extends AwesomeActivity {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开启沉浸式
        setStatusBarTranslucent(true);

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
            drawerFragment.setMenuFragment(new MenuFragment());
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMaxDrawerWidth(300); // 设置 menu 的最大宽度
            //drawerFragment.setMinDrawerMargin(0); // 可使 menu 和 drawerLayout 同宽

            setRootFragment(drawerFragment);
        }
    }

    @Override
    protected void onCustomStyle(Style style) {
        // style.setTitleGravity(Gravity.CENTER);
        // style.setBottomBarBackgroundColor("#3F51B5");
        // style.setBottomBarBackgroundColor("#FDFFFFFF");
    }
}
