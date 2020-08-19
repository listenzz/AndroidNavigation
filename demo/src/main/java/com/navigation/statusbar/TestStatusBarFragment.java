package com.navigation.statusbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.BarStyle;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.NavigationFragment;
import com.navigation.androidx.Style;
import com.navigation.androidx.ToolbarButtonItem;
import com.navigation.toolbar.NoToolbarFragment;

/**
 * Created by listen on 2018/1/12.
 */

public class TestStatusBarFragment extends BaseFragment {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setStatusBarStyle(BarStyle.LightContent);
    }

    @Override
    public void appendStatusBarPadding(View view) {
        if (!isInDialog()) {
            super.appendStatusBarPadding(view);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status_bar, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.pop_to_root).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                navigationFragment.popToRootFragment();
            }
        });

        root.findViewById(R.id.status_bar_style).setOnClickListener(v -> requireNavigationFragment().pushFragment(new StatusBarStyleFragment()));


        root.findViewById(R.id.status_bar_hidden).setOnClickListener(v -> requireNavigationFragment().pushFragment(new StatusBarHiddenFragment()));


        root.findViewById(R.id.status_bar_color).setOnClickListener(v -> requireNavigationFragment().pushFragment(new StatusBarColorFragment()));

        root.findViewById(R.id.no_toolbar).setOnClickListener(v -> requireNavigationFragment().pushFragment(new NoToolbarFragment()));

        root.findViewById(R.id.custom).setOnClickListener(v -> requireNavigationFragment().pushFragment(new CustomStatusBarFragment()));

        if (isNavigationRoot()) {
            root.findViewById(R.id.pop_to_root).setEnabled(false);
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle("状态栏");
        if (isNavigationRoot()) {
            String iconUri = "font://FontAwesome/" + fromCharCode(61641) + "/24";
            ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
            builder.icon(iconUri).listener(view -> {
                DrawerFragment drawerFragment = getDrawerFragment();
                if (drawerFragment != null) {
                    drawerFragment.toggleMenu();
                }
            });
            setLeftBarButtonItem(builder.build());
        }
    }
}
