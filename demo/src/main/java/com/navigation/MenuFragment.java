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
            getNavigationFragment().pushFragment(new ToolbarColorTransitionFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.coordinator).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new CoordinatorFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.search).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new SearchFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.view_pager).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new ViewPagerFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.shared_element).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new GridFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.web).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new WebFragment());
            getDrawerFragment().closeMenu();
        });

        root.findViewById(R.id.dialog).setOnClickListener(v -> {
            getNavigationFragment().pushFragment(new DialogEntryFragment());
            getDrawerFragment().closeMenu();
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void onStatusBarTranslucentChanged(boolean translucent) {
        super.onStatusBarTranslucentChanged(translucent);
        changeRootViewTopPadding(translucent);
    }

    private void changeRootViewTopPadding(boolean translucent) {
        if (AppUtils.isCutout(requireActivity())) {
            if (translucent) {
                appendStatusBarPadding(getView(), getView().getLayoutParams().height );
            } else {
                removeStatusBarPadding(getView(), getView().getLayoutParams().height);
            }
        }
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
