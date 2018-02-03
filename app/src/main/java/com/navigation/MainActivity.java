package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;

import com.navigation.library.AwesomeActivity;
import com.navigation.library.DrawerFragment;
import com.navigation.library.NavigationFragment;
import com.navigation.library.Style;
import com.navigation.library.TabBarFragment;
import com.navigation.library.TabBarItem;

public class MainActivity extends AwesomeActivity {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            TestFragment testFragment = new TestFragment();
            NavigationFragment navigation = new NavigationFragment();
            navigation.setRootFragment(testFragment);
            String navigationIcon = "font://FontAwesome/" + fromCharCode(61732) + "/24";
            navigation.setTabBarItem(new TabBarItem(navigationIcon, "Navigation"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            String styleIcon = "flower";
            statusBar.setTabBarItem(new TabBarItem(styleIcon, "Status"));

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
       //  style.setStatusBarColor(Color.parseColor("#303F9F"));
        style.setToolbarBackgroundColor(Color.parseColor("#3F51B5"));
        style.setTitleGravity(Gravity.CENTER);
        // style.setBottomBarBackgroundColor("#3F51B5");
    }
}
