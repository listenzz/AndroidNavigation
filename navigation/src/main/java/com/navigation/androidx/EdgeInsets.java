package com.navigation.androidx;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;

public class EdgeInsets {
    public int left;
    public int top;
    public int right;
    public int bottom;


    public static EdgeInsets of(Insets insets) {
        return new EdgeInsets(insets.left, insets.top, insets.right, insets.bottom);
    }

    public EdgeInsets() {

    }

    public EdgeInsets(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void plus(Insets insets) {
        left += insets.left;
        top += insets.top;
        right += insets.right;
        bottom += insets.bottom;
    }

    @NonNull
    @Override
    public String toString() {
        return "EdgeInsets{" +
                "left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", bottom=" + bottom +
                '}';
    }
}
