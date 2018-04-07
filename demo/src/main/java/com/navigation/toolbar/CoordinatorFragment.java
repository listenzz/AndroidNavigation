package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;
import me.listenzz.navigation.AwesomeToolbar;


/**
 * Created by Listen on 2018/2/1.
 */

public class CoordinatorFragment extends AwesomeFragment {

    Toolbar toolbar;

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return null;
    }

    @Override
    protected int preferredStatusBarColor() {
        if (isStatusBarTranslucent()) {
            return Color.TRANSPARENT;
        } else {
            return super.preferredStatusBarColor();
        }
    }

    @Override
    protected boolean preferredStatusBarColorAnimated() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_coordinator, container, false);

        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(style.getBackIcon());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNavigationFragment().popFragment();
            }
        });

        // important
        if (isStatusBarTranslucent()) {
            appendStatusBarPadding(toolbar, getToolbarHeight());
        }

        return root;
    }

}
