package com.navigation.androidx;

public class EdgeInsets {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public EdgeInsets() {

    }

    public EdgeInsets(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

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
