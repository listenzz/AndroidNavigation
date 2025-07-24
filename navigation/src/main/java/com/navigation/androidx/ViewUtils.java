package com.navigation.androidx;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewUtils {

    public interface ViewCallback {
        void invoke(View view);
    }

    public static void doOnPreDrawOnce(@NonNull View view, @NonNull final ViewCallback callback) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                callback.invoke(view);
                return false;
            }
        });
    }

    public static void applyWindowInsets(@NonNull Window window, @NonNull View view, @NonNull ViewCallback callback) {
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsets != null) {
            doOnPreDrawOnce(view, callback);
        }

        view.setOnApplyWindowInsetsListener((v, insets) -> {
            doOnPreDrawOnce(v, callback);
            return insets;
        });
    }
}
