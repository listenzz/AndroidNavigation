package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;
import com.navigation.androidx.SystemUI;
import com.navigation.statusbar.TestStatusBarFragment;

public class NoToolbarFragment extends TestStatusBarFragment {

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
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        SystemUI.applyStatusBarPaddingIfNeeded(getWindow(), root);
    }

    @Override
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return null;
    }

}
