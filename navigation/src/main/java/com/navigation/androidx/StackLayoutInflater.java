package com.navigation.androidx;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class StackLayoutInflater extends LayoutInflater {

    private final LayoutInflater mLayoutInflater;

    public StackLayoutInflater(Context context, LayoutInflater layoutInflater) {
        super(context);
        mLayoutInflater = layoutInflater;
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return mLayoutInflater.cloneInContext(context);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mLayoutInflater.inflate(resource, frameLayout, true);
        return frameLayout;
    }
}
