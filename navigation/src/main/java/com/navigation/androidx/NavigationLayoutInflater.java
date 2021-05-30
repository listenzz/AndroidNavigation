package com.navigation.androidx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class NavigationLayoutInflater extends LayoutInflater {

    public static final String TAG = "Navigation";

    private final LayoutInflater layoutInflater;

    public NavigationLayoutInflater(Context context, LayoutInflater layoutInflater) {
        super(context);
        this.layoutInflater = layoutInflater;
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return layoutInflater.cloneInContext(context);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        layoutInflater.inflate(resource, frameLayout, true);
        return frameLayout;
    }
}
