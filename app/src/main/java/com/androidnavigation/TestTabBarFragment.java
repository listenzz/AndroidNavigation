package com.androidnavigation;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.androidnavigation.fragment.AwesomeFragment;
import com.androidnavigation.fragment.TabBarFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Listen on 2018/1/12.
 */

public class TestTabBarFragment extends TabBarFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<AwesomeFragment> fragments = new ArrayList<>();
        fragments.add(new TestNavigationFragment());
        fragments.add(new TestNavigationFragment());
        setFragments(fragments);
    }
}
