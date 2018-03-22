package com.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.navigation.dialog.DialogEntryFragment;
import com.navigation.sharedelement.GridFragment;
import com.navigation.toolbar.CoordinatorFragment;
import com.navigation.toolbar.ToolBarColorTransitionFragment;
import com.navigation.toolbar.ViewPagerFragment;

import me.listenzz.navigation.AwesomeFragment;

/**
 * Created by listen on 2018/1/13.
 */

public class MenuFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.toolbar_color_transition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new ToolBarColorTransitionFragment());

            }
        });

        root.findViewById(R.id.coordinator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new CoordinatorFragment());
            }
        });

        root.findViewById(R.id.view_pager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new ViewPagerFragment());
            }
        });

        root.findViewById(R.id.shared_element).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new GridFragment());
            }
        });

        root.findViewById(R.id.web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new WebFragment());
            }
        });

        root.findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerFragment().closeMenu();
                getNavigationFragment().pushFragment(new DialogEntryFragment());
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


}
