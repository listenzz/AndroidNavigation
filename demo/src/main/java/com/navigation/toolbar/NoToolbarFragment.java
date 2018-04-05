package com.navigation.toolbar;

import android.graphics.Color;
import android.view.View;

import com.navigation.statusbar.TestStatusBarFragment;

import me.listenzz.navigation.AwesomeToolbar;

/**
 * Created by Listen on 2018/2/1.
 */

public class NoToolbarFragment extends TestStatusBarFragment {

    @Override
    protected int preferredStatusBarColor() {
        return Color.argb(50, 0,0,0);
    }

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return null;
    }
}
