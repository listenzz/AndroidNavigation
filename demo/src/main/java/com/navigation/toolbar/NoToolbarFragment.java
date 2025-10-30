package com.navigation.toolbar;

import android.view.View;

import androidx.annotation.NonNull;

import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;
import com.navigation.statusbar.TestStatusBarFragment;

public class NoToolbarFragment extends TestStatusBarFragment {

    @NonNull
    @Override
    protected BarStyle preferredStatusBarStyle() {
        return BarStyle.DarkContent;
    }

    @Override
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return null;
    }

}
