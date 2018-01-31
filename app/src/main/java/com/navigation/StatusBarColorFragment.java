package com.navigation;

import android.graphics.Color;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarColorFragment extends TestStatusBarFragment {

    @Override
    protected int preferredStatusBarColor() {
        return Color.RED;
    }


    @Override
    protected boolean preferredStatusBarColorAnimated() {
        return true;
    }
}
