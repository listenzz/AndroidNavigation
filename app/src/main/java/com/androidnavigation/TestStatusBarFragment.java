package com.androidnavigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidnavigation.fragment.AwesomeFragment;
import com.androidnavigation.fragment.DrawerFragment;

/**
 * Created by listen on 2018/1/12.
 */

public class TestStatusBarFragment extends AwesomeFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status_bar, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.toggle_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerFragment drawerFragment = getDrawerFragment();
                if (drawerFragment != null) {
                    drawerFragment.toggleMenu();
                }
            }
        });

        root.findViewById(R.id.status_bar_style_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarStyle("dark-content");
            }
        });

        root.findViewById(R.id.status_bar_style_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarStyle("light-content");
            }
        });

        root.findViewById(R.id.status_bar_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarHidden(true);
            }
        });

        root.findViewById(R.id.status_bar_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarHidden(false);
            }
        });

        root.findViewById(R.id.status_bar_translucent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarTranslucent(true);
            }
        });

        root.findViewById(R.id.status_bar_opacity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarTranslucent(false);
            }
        });


        root.findViewById(R.id.status_bar_color_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarColor(Color.RED, true);
            }
        });

        root.findViewById(R.id.status_bar_color_transparent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusBarColor(Color.TRANSPARENT, true);
            }
        });

        root.findViewById(R.id.status_bar_append_padding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendStatusBarPaddingAndHeight(getTopBar(), getTopBarHeight());
            }
        });

        root.findViewById(R.id.status_bar_remove_padding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeStatusBarPaddingAndHeight(getTopBar(), getTopBarHeight());
            }
        });

        return root;
    }
}
