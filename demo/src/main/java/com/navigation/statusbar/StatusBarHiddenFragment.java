package com.navigation.statusbar;

import android.graphics.Color;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarHiddenFragment extends TestStatusBarFragment {

    @Override
    protected boolean preferredStatusBarHidden() {
        return isStatusBarTranslucent();
    }

    @Override
    protected int preferredStatusBarColor() {
        return Color.TRANSPARENT;
    }
}
