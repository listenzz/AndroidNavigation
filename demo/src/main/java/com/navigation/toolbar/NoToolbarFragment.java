package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;
import com.navigation.statusbar.TestStatusBarFragment;

/**
 * Created by Listen on 2018/2/1.
 */

public class NoToolbarFragment extends TestStatusBarFragment {

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        appendStatusBarPadding(root);
    }

    @NonNull
    @Override
    protected BarStyle preferredStatusBarStyle() {
        return BarStyle.DarkContent;
    }

    @Override
    protected int preferredStatusBarColor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Color.parseColor("#4A4A4A");
        }
        return Color.TRANSPARENT;
    }

    @Override
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return null;
    }
}
