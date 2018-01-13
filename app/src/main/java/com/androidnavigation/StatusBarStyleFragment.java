package com.androidnavigation;

import android.graphics.Color;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarStyleFragment extends TestStatusBarFragment {


    @Override
    protected String preferredStatusBarStyle() {
        return "dark-content";
    }

    @Override
    protected int prefersStatusBarColor() {
        return Color.GREEN;
    }
}
