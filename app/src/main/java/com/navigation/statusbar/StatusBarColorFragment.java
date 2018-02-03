package com.navigation.statusbar;

import android.graphics.Color;

import com.navigation.library.Style;

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
