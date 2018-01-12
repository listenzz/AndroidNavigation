package com.androidnavigation;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.androidnavigation.fragment.DrawerFragment;

/**
 * Created by Listen on 2018/1/12.
 */

public class TestDrawerFragment extends DrawerFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentFragment(new TestTabBarFragment());
        setMenuFragment(new TestFragment());
    }
}
