package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    protected boolean preferredStatusBarColorAnimated() {
        return false;
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
                getAwesomeToolbar().setAlpha(progress / 100.0f);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAwesomeToolbar().setAlpha(0);
    }
}
