package com.navigation.androidx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

public class AppUtils {

    private static final String TAG = "Navigation";

    private AppUtils() {
    }

    public static boolean isBlackColor(int color, int level) {
        int grey = toGrey(color);
        return grey < level;
    }

    public static int toGrey(int rgb) {
        int blue = rgb & 0x000000FF;
        int green = (rgb & 0x0000FF00) >> 8;
        int red = (rgb & 0x00FF0000) >> 16;
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    public static ColorStateList buttonColorStateList(int tintColor) {
        int disableColor = ColorUtils.setAlphaComponent(AppUtils.toGrey(tintColor), 100);
        int pressedColor = ColorUtils.setAlphaComponent(tintColor, 150);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},  // pressed
                new int[]{android.R.attr.state_enabled},  // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
        };
        int[] colors = new int[]{
                pressedColor,
                tintColor,
                disableColor,
        };
        return new ColorStateList(states, colors);
    }

    public static String colorToString(@ColorInt int color) {
        return String.format("#%08X", color);
    }

    public static Bitmap createBitmapFromView(View view) {
        view.clearFocus();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            canvas.save();
            canvas.drawColor(Color.TRANSPARENT);
            view.draw(canvas);
            canvas.restore();
            canvas.setBitmap(null);
        }
        return bitmap;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static int fetchContextColor(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int fetchContextDimension(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int dimension = a.getDimensionPixelOffset(0, 0);
        a.recycle();
        return dimension;
    }

    public static int fetchContextResource(Context context, int androidAttribute) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{androidAttribute});
        int resource = a.getResourceId(0, 0);
        a.recycle();
        return resource;
    }

    public static int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static void hideSoftInput(Window window) {
        if (window == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        View view = window.getCurrentFocus();
        if (view == null) {
            return;
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static Drawable copyDrawable(Drawable drawable) {
        return DrawableCompat.wrap(drawable.getConstantState().newDrawable()).mutate();
    }

}
