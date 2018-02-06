package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;


/**
 * Created by Listen on 2018/2/1.
 */

public class CoordinatorFragment extends AwesomeFragment {

    Toolbar toolbar;

    @Override
    protected Toolbar onCreateToolbar(View parent) {
        return toolbar;
    }

    @Override
    protected int preferredStatusBarColor() {
        if (isContentUnderStatusBar()) {
            return Color.TRANSPARENT;
        } else {
            return super.preferredStatusBarColor();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_coordinator, container, false);

        toolbar = root.findViewById(R.id.toolbar);

        // important
        if (isContentUnderStatusBar()) {
            appendStatusBarPadding(toolbar, getToolbarHeight());
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toolbar.setNavigationIcon(style.getBackIcon());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationFragment().popFragment();
            }
        });

        setToolbarRightButton(ContextCompat.getDrawable(getContext(), android.R.drawable.stat_notify_chat), "Chat", true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



}
