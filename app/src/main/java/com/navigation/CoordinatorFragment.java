package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.fragment.AwesomeFragment;

/**
 * Created by Listen on 2018/2/1.
 */

public class CoordinatorFragment extends AwesomeFragment {

    @Override
    protected boolean shouldAutoCreateToolBar() {
        return false;
    }

    @Override
    protected int preferredStatusBarColor() {
        return Color.TRANSPARENT;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_coordinator, container, false);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.nav_ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationFragment().popFragment();
            }
        });

        appendStatusBarPaddingAndHeight(toolbar, getToolBarHeight());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



}
