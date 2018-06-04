package me.listenzz.navigation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DialogLayoutInflater extends LayoutInflater {

    public static final String TAG = "Navigation";

    private LayoutInflater layoutInflater;

    private DialogFrameLayout.OnTouchOutsideListener listener;

    private boolean assumeNoHit;

    public DialogLayoutInflater(Context context, LayoutInflater layoutInflater, DialogFrameLayout.OnTouchOutsideListener listener, boolean assumeNoHit) {
        super(context);
        this.layoutInflater = layoutInflater;
        this.listener = listener;
        this.assumeNoHit = assumeNoHit;
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return layoutInflater.cloneInContext(context);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        DialogFrameLayout dialogFrameLayout = new DialogFrameLayout(getContext());
        dialogFrameLayout.setOnTouchOutsideListener(listener);
        dialogFrameLayout.setAssumeNoHit(assumeNoHit);
        dialogFrameLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        layoutInflater.inflate(resource, dialogFrameLayout, true);
        return dialogFrameLayout;
    }
}
