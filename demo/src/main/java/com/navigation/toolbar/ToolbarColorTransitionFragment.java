package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.Style;


/**
 * Created by Listen on 2018/2/1.
 */

public class ToolbarColorTransitionFragment extends BaseFragment {

    @Override
    protected int preferredStatusBarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setToolbarBackgroundColor(Color.parseColor("#EE6413"));
    }

    @Override
    protected boolean extendedLayoutIncludesToolbar() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_toolbar_color_transition, container, false);
        SeekBar seekBar = root.findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                getToolbar().setAlpha(progress / 100.0f);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().setAlpha(0);
    }
}
