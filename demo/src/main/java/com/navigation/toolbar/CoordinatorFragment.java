package com.navigation.toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.BaseFragment;
import com.navigation.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.Style;


/**
 * Created by Listen on 2018/2/1.
 */

public class CoordinatorFragment extends BaseFragment {

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
        return Color.TRANSPARENT;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_coordinator, container, false);

        toolbar = root.findViewById(R.id.toolbar);

        // important
        appendStatusBarPadding(toolbar);

        return root;
    }

}
