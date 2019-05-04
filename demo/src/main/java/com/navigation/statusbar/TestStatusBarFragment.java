package com.navigation.statusbar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.toolbar.NoToolbarFragment;

import me.listenzz.navigation.BarStyle;
import me.listenzz.navigation.DrawerFragment;
import me.listenzz.navigation.NavigationFragment;
import me.listenzz.navigation.Style;
import me.listenzz.navigation.ToolbarButtonItem;

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
    protected boolean hidesBottomBarWhenPushed() {
        return false;
    }

    @Override
    public void appendStatusBarPadding(View view, int viewHeight) {
        if (!isInDialog()) {
            super.appendStatusBarPadding(view, viewHeight);
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

        root.findViewById(R.id.status_bar_style).setOnClickListener(v -> getNavigationFragment().pushFragment(new StatusBarStyleFragment()));


        root.findViewById(R.id.status_bar_hidden).setOnClickListener(v -> getNavigationFragment().pushFragment(new StatusBarHiddenFragment()));


        root.findViewById(R.id.status_bar_color).setOnClickListener(v -> getNavigationFragment().pushFragment(new StatusBarColorFragment()));

        root.findViewById(R.id.no_toolbar).setOnClickListener(v -> getNavigationFragment().pushFragment(new NoToolbarFragment()));

        root.findViewById(R.id.custom).setOnClickListener(v -> getNavigationFragment().pushFragment(new CustomStatusBarFragment()));

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
