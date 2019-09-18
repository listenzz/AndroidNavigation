package com.navigation.androidx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class DialogLayoutInflater extends LayoutInflater {

    public static final String TAG = "Navigation";

    private LayoutInflater layoutInflater;

    private DialogFrameLayout.OnTouchOutsideListener listener;

    public DialogLayoutInflater(Context context, LayoutInflater layoutInflater, DialogFrameLayout.OnTouchOutsideListener listener) {
        super(context);
        this.layoutInflater = layoutInflater;
        this.listener = listener;
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return layoutInflater.cloneInContext(context);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        DialogFrameLayout dialogFrameLayout = new DialogFrameLayout(getContext());
        dialogFrameLayout.setOnTouchOutsideListener(listener);
        dialogFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        layoutInflater.inflate(resource, dialogFrameLayout, true);
        return dialogFrameLayout;
    }
}
