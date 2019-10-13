package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.navigation.androidx.AppUtils;
import com.navigation.androidx.AwesomeActivity;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.NavigationFragment;
import com.navigation.androidx.Style;
import com.navigation.androidx.TabBarFragment;
import com.navigation.androidx.TabBarItem;
import com.navigation.statusbar.TestStatusBarFragment;

import androidx.annotation.NonNull;

public class MainActivity extends AwesomeActivity {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarTranslucent(true);

        if (savedInstanceState == null) {

            TestNavigationFragment testNavigationFragment = new TestNavigationFragment();
            NavigationFragment navigation = new NavigationFragment();
            navigation.setRootFragment(testNavigationFragment);
            String iconUri = "font://FontAwesome/" + fromCharCode(61732) + "/24";
            navigation.setTabBarItem(new TabBarItem(iconUri, "导航"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            statusBar.setTabBarItem(new TabBarItem(R.drawable.flower, "状态栏"));

            TabBarFragment tabBarFragment = new TabBarFragment();
            tabBarFragment.setChildFragments(navigation, statusBar);

            DrawerFragment drawerFragment = new DrawerFragment();
            drawerFragment.setMenuFragment(new MenuFragment());
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMaxDrawerWidth(300); // 设置 menu 的最大宽度
            //drawerFragment.setMinDrawerMargin(0); // 可使 menu 和 drawerLayout 同宽

            setActivityRootFragment(drawerFragment);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG, "onAttachedToWindow 是否刘海眉：" + AppUtils.isCutout(this));
        // 设置沉浸式
        // setStatusBarTranslucent(!AppUtils.isCutout(this));
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        // style.setTitleGravity(Gravity.CENTER);
        // style.setTabBarBackgroundColor("#3F51B5");
        // style.setTabBarBackgroundColor("#FDFFFFFF");
        style.setScreenBackgroundColor(Color.parseColor("#EDEDED"));
        style.setSwipeBackEnabled(true); // 开启手势返回
    }
}
