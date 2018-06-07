package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;
import me.listenzz.navigation.AwesomeToolbar;
import me.listenzz.navigation.Style;


/**
 * Created by Listen on 2018/2/1.
 */

public class CoordinatorFragment extends AwesomeFragment {

    AwesomeToolbar toolbar;

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return toolbar;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setShadow(null);
        style.setToolbarBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected int preferredStatusBarColor() {
        if (isStatusBarTranslucent()) {
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
        if (isStatusBarTranslucent()) {
            appendStatusBarPadding(toolbar, getToolbarHeight());
        }

        return root;
    }

}
