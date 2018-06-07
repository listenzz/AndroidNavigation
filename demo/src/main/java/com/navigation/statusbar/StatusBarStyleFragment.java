package com.navigation.statusbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import me.listenzz.navigation.BarStyle;
import me.listenzz.navigation.Style;
import me.listenzz.navigation.ToolbarButtonItem;


/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarStyleFragment extends TestStatusBarFragment {

    private BarStyle barStyle = BarStyle.DarkContent;

    @Override
    protected BarStyle preferredStatusBarStyle() {
        return barStyle;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setStatusBarStyle(BarStyle.DarkContent);
        style.setStatusBarColor(Color.WHITE);
        style.setToolbarBackgroundColor(Color.WHITE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRightBarButtonItem(new ToolbarButtonItem(null, 0, "切换", Color.RED, true, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferredStatusBarStyle() == BarStyle.DarkContent) {
                    barStyle = BarStyle.LightContent;
                } else {
                    barStyle = BarStyle.DarkContent;
                }
                setNeedsStatusBarAppearanceUpdate();
            }
        }));
    }

}
