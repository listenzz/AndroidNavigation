package com.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.StackFragment;
import com.navigation.androidx.SystemUI;
import com.navigation.androidx.TabBarFragment;
import com.navigation.dialog.DialogEntryFragment;
import com.navigation.sharedelement.GridFragment;
import com.navigation.toolbar.CoordinatorFragment;
import com.navigation.toolbar.SearchFragment;
import com.navigation.toolbar.ToolbarColorTransitionFragment;
import com.navigation.toolbar.ViewBindingFragment;
import com.navigation.toolbar.ViewPagerFragment;

public class MenuFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.toolbar_color_transition).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new ToolbarColorTransitionFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.coordinator).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new CoordinatorFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.search).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new SearchFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.view_pager).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new ViewPagerFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.shared_element).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new GridFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.web).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new WebFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.dialog).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new DialogEntryFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.view_binding).setOnClickListener(v -> {
            requireStackFragment().pushFragment(new ViewBindingFragment());
            requireDrawerFragment().closeMenu();
        });

        SystemUI.applyStatusBarPaddingIfNeeded(getWindow(), root);

        return root;
    }

    @Override
    public StackFragment getStackFragment() {
        DrawerFragment drawerFragment = getDrawerFragment();
        if (drawerFragment == null) {
            return super.getStackFragment();
        }

        TabBarFragment tabBarFragment = drawerFragment.requireContentFragment().getTabBarFragment();
        if (tabBarFragment == null) {
            return drawerFragment.requireContentFragment().getStackFragment();
        }
        return tabBarFragment.requireSelectedFragment().getStackFragment();
    }
}
