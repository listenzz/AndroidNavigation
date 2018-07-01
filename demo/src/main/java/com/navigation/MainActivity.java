package com.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;

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
            navigation.setSwipeBackEnabled(true); // 开启侧滑返回
            navigation.setRootFragment(testNavigationFragment);
            String iconUri = "font://FontAwesome/" + fromCharCode(61732) + "/24";
            navigation.setTabBarItem(new TabBarItem(iconUri, "Navigation"));

            TestStatusBarFragment testStatusBarFragment = new TestStatusBarFragment();
            NavigationFragment statusBar = new NavigationFragment();
            statusBar.setRootFragment(testStatusBarFragment);
            statusBar.setTabBarItem(new TabBarItem(R.drawable.flower, "Status"));

            TabBarFragment tabBarFragment = new TabBarFragment();
            tabBarFragment.setChildFragments(navigation, statusBar);

            DrawerFragment drawerFragment = new DrawerFragment();
            drawerFragment.setMenuFragment(new MenuFragment());
            drawerFragment.setContentFragment(tabBarFragment);
            drawerFragment.setMaxDrawerWidth(300); // 设置 menu 的最大宽度
            //drawerFragment.setMinDrawerMargin(0); // 可使 menu 和 drawerLayout 同宽
            drawerFragment.setMenuInteractive(false); // 是否可以侧滑打开抽屉，如果 NavigationFragment 也开启了侧滑返回，它们之间会冲突


//            CustomContainerFragment customContainerFragment = new CustomContainerFragment();
//            customContainerFragment.setFirsFloorFragment(testNavigationFragment);
//
//            drawerFragment.setContentFragment(customContainerFragment);

            setActivityRootFragment(drawerFragment);
        }
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        // style.setTitleGravity(Gravity.CENTER);
        // style.setTabBarBackgroundColor("#3F51B5");
        // style.setTabBarBackgroundColor("#FDFFFFFF");
    }
}
