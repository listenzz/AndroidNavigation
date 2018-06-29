package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Listen on 2018/06/29.
 */
public class RetainFragment extends Fragment {

    private TabBarProvider tabBarProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setTabBarProvider(TabBarProvider tabBarProvider) {
        this.tabBarProvider = tabBarProvider;
    }

    public TabBarProvider getTabBarProvider() {
        return tabBarProvider;
    }

}
