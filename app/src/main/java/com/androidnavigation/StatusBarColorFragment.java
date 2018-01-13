package com.androidnavigation;

import android.graphics.Color;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarColorFragment extends TestStatusBarFragment {

    @Override
    protected int prefersStatusBarColor() {
        return Color.RED;
    }


    @Override
    protected boolean prefersStatusBarColorAnimated() {
        return true;
    }
}
