package com.navigation.androidx;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class DialogLayoutInflater extends LayoutInflater {

    public static final String TAG = "Navigation";

    private final LayoutInflater mLayoutInflater;

    private final DialogFrameLayout.OnTouchOutsideListener mListener;

    public DialogLayoutInflater(Context context, LayoutInflater layoutInflater, DialogFrameLayout.OnTouchOutsideListener listener) {
        super(context);
        mLayoutInflater = layoutInflater;
        mListener = listener;
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return mLayoutInflater.cloneInContext(context);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        DialogFrameLayout dialogFrameLayout = new DialogFrameLayout(getContext());
        dialogFrameLayout.setOnTouchOutsideListener(mListener);
        dialogFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mLayoutInflater.inflate(resource, dialogFrameLayout, true);
        return dialogFrameLayout;
    }
}
