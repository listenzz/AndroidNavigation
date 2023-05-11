package com.navigation.androidx;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ViewUtils {

    public interface OnPreDrawListener {
        void onPreDraw(View view, LayoutProps initialPadding);
    }

    public static class LayoutProps {
        public int start;
        public int top;
        public int end;
        public int bottom;
        public int height;

        public LayoutProps(int start, int top, int end, int bottom, int height) {
            this.start = start;
            this.top = top;
            this.end = end;
            this.bottom = bottom;
            this.height = height;
        }
    }

    public static void doOnPreDrawOnce(@NonNull View view, @Nullable WindowInsetsCompat windowInsets, @NonNull final OnPreDrawListener listener) {
        final LayoutProps initialPadding =
                new LayoutProps(
                        ViewCompat.getPaddingStart(view),
                        view.getPaddingTop(),
                        ViewCompat.getPaddingEnd(view),
                        view.getPaddingBottom(),
                        view.getLayoutParams().height);

        if (windowInsets != null) {
            listener.onPreDraw(view, initialPadding);
        }

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                listener.onPreDraw(view, initialPadding);
                return true;
            }
        });
    }
}
