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
import com.navigation.androidx.StackFragment;
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
        if (getDialogAwesomeFragment() == null) {
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
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                stackFragment.popToRootFragment();
            }
        });

        root.findViewById(R.id.status_bar_style).setOnClickListener(v -> requireStackFragment().pushFragment(new StatusBarStyleFragment()));

        root.findViewById(R.id.status_bar_hidden).setOnClickListener(v -> requireStackFragment().pushFragment(new StatusBarHiddenFragment()));
        
        root.findViewById(R.id.status_bar_color).setOnClickListener(v -> requireStackFragment().pushFragment(new StatusBarColorFragment()));

        root.findViewById(R.id.no_toolbar).setOnClickListener(v -> requireStackFragment().pushFragment(new NoToolbarFragment()));

        root.findViewById(R.id.custom).setOnClickListener(v -> requireStackFragment().pushFragment(new CustomSystemUIFragment()));

        if (isStackRoot()) {
            root.findViewById(R.id.pop_to_root).setEnabled(false);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("状态栏");
        if (isStackRoot()) {
            String iconUri = "font://FontAwesome/" + fromCharCode(61641) + "/24";
            ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
            builder.icon(iconUri).listener(v -> {
                DrawerFragment drawerFragment = getDrawerFragment();
                if (drawerFragment != null) {
                    drawerFragment.toggleMenu();
                }
            });
            setLeftBarButtonItem(builder.build());
        }
    }
}
