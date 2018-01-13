package com.androidnavigation;

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
                getNavigationFragment().pushFragment(new StatusBarStyleFragment());
            }
        });


        root.findViewById(R.id.status_bar_hidden).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getNavigationFragment().pushFragment(new StatusBarHiddenFragment());
            }
        });


        root.findViewById(R.id.status_bar_color_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationFragment().pushFragment(new StatusBarColorFragment());
            }
        });



        return root;
    }
}
