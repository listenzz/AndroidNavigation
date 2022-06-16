package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.navigation.androidx.AwesomeActivity;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.StackFragment;
import com.navigation.androidx.Style;
import com.navigation.androidx.SystemUI;
import com.navigation.androidx.TabBarFragment;
import com.navigation.androidx.TabBarItem;
import com.navigation.statusbar.TestStatusBarFragment;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;

public class MainActivity extends AwesomeActivity {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        Log.i(TAG, "MainActivity#onCreate");
        if (savedInstanceState == null) {

            StackFragment navigation = new StackFragment();
            navigation.setRootFragment(new TestNavigationFragment());
            String iconUri = "font://FontAwesome/" + fromCharCode(61732) + "/24";
            navigation.setTabBarItem(new TabBarItem("导航", iconUri));

            StackFragment statusBar = new StackFragment();
            statusBar.setRootFragment(new TestStatusBarFragment());
            statusBar.setTabBarItem(new TabBarItem("状态栏", R.drawable.flower));

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
        Log.i(TAG, "onAttachedToWindow 是否刘海眉：" + SystemUI.isCutout(this));
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        // style.setTitleGravity(Gravity.CENTER);
        // style.setTabBarBackgroundColor("#3F51B5");
        // style.setTabBarBackgroundColor("#FDFFFFFF");
        style.setScreenBackgroundColor(Color.parseColor("#EDEDED"));
        style.setSwipeBackEnabled(true); // 开启手势返回
        style.setNavigationBarColor(Color.WHITE);
    }
}
