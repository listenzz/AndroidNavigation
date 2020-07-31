package com.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.androidx.AppUtils;
import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.NavigationFragment;
import com.navigation.androidx.TabBarFragment;
import com.navigation.dialog.DialogEntryFragment;
import com.navigation.sharedelement.GridFragment;
import com.navigation.toolbar.CoordinatorFragment;
import com.navigation.toolbar.SearchFragment;
import com.navigation.toolbar.ToolbarColorTransitionFragment;
import com.navigation.toolbar.ViewPagerFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by listen on 2018/1/13.
 */

public class MenuFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.toolbar_color_transition).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new ToolbarColorTransitionFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.coordinator).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new CoordinatorFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.search).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new SearchFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.view_pager).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new ViewPagerFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.shared_element).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new GridFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.web).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new WebFragment());
            requireDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.dialog).setOnClickListener(v -> {
            requireNavigationFragment().pushFragment(new DialogEntryFragment());
            requireDrawerFragment().closeMenu();
        });

        appendStatusBarPadding(root);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public NavigationFragment getNavigationFragment() {
        DrawerFragment drawerFragment = getDrawerFragment();
        if (drawerFragment != null) {
            TabBarFragment tabBarFragment = drawerFragment.getContentFragment().getTabBarFragment();
            if (tabBarFragment != null) {
                return tabBarFragment.getSelectedFragment().getNavigationFragment();
            }
            return drawerFragment.getContentFragment().getNavigationFragment();
        }
        return super.getNavigationFragment();
    }
}
