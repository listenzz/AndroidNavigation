package com.navigation.androidx;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

public class ViewUtils {

    public interface OnPreDrawListener {
        void onPreDraw(View view);
    }

    public static void doOnPreDrawOnce(@NonNull View view, @NonNull final OnPreDrawListener listener) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                listener.onPreDraw(view);
                return true;
            }
        });
    }
}
