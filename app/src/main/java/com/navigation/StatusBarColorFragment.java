package com.navigation;

import android.graphics.Color;

import com.navigation.fragment.Style;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarColorFragment extends TestStatusBarFragment {

    @Override
    protected void onCustomStyle(Style style) {
        style.setToolbarBackgroundColor(Color.DKGRAY);
    }

    @Override
    protected int preferredStatusBarColor() {
        return Color.RED;
    }


}
