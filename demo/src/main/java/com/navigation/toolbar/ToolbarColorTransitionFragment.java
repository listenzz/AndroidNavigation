package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;
import me.listenzz.navigation.Style;


/**
 * Created by Listen on 2018/2/1.
 */

public class ToolbarColorTransitionFragment extends AwesomeFragment {

    @Override
    protected int preferredStatusBarColor() {
        if (isStatusBarTranslucent()) {
            return Color.TRANSPARENT;
        } else {
            return super.preferredStatusBarColor();
        }
    }

    @Override
    protected Toolbar onCreateToolbar(View parent) {
        Toolbar toolbar =  super.onCreateToolbar(parent);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        return  toolbar;
    }

    @Override
    protected void onCustomStyle(Style style) {
       style.setToolbarBackgroundColor(Color.parseColor("#EE6413"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_toolbar_color_transition, container, false);
        SeekBar seekBar = root.findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int alpha = progress * 255 / 100;
                int color = ColorUtils.setAlphaComponent(style.getToolbarBackgroundColor(), alpha);
                style.setToolbarBackgroundColor(color);
                setNeedsToolbarAppearanceUpdate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return root;
    }

}
