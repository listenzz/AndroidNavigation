package com.navigation.statusbar;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;

import com.navigation.androidx.BarStyle;
import com.navigation.androidx.Style;

public class StatusBarStyleFragment extends TestStatusBarFragment {

    @NonNull
    @Override
    protected BarStyle preferredStatusBarStyle() {
        return BarStyle.DarkContent;
    }

    @Override
    protected int preferredStatusBarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setToolbarBackgroundColor(Color.WHITE);
    }

}
