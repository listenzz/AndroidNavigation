package com.navigation.statusbar;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;

import com.navigation.androidx.BarStyle;
import com.navigation.androidx.Style;


/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarStyleFragment extends TestStatusBarFragment {

    private BarStyle barStyle = BarStyle.DarkContent;

    @NonNull
    @Override
    protected BarStyle preferredStatusBarStyle() {
        return barStyle;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setStatusBarStyle(BarStyle.DarkContent);
        style.setStatusBarColor(Color.TRANSPARENT);
        style.setToolbarBackgroundColor(Color.WHITE);
    }

    @Override
    protected int preferredStatusBarColor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Color.parseColor("#4A4A4A");
        }
        return Color.TRANSPARENT;
    }

}
