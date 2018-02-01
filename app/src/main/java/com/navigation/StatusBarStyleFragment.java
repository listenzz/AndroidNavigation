package com.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.navigation.fragment.BarStyle;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarStyleFragment extends TestStatusBarFragment {

    private BarStyle barStyle = BarStyle.DarkContent;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarRightButton(null, "切换", true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferredStatusBarStyle() == BarStyle.DarkContent) {
                    barStyle = BarStyle.LightContent;
                } else {
                    barStyle = BarStyle.DarkContent;
                }
                setNeedsStatusBarAppearanceUpdate();
            }
        });

    }

    @Override
    protected BarStyle preferredStatusBarStyle() {
        return barStyle;
    }

}
