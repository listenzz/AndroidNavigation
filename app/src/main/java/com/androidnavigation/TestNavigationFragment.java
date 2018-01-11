package com.androidnavigation;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.androidnavigation.fragment.NavigationFragment;

/**
 * Created by Listen on 2018/1/11.
 */

public class TestNavigationFragment extends NavigationFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRootFragment(new TestFragment());
    }
}
