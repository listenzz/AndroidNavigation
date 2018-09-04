package me.listenzz.navigation;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.TextView;

/**
 * Created by listen on 2018/4/5.
 */

public class ToolbarButtonItem {

    public final String title;
    public final String iconUri;
    @DrawableRes
    public final int iconRes;
    public final boolean enabled;
    public final int tintColor;
    public final View.OnClickListener onClickListener;

    public ToolbarButtonItem(String iconUri, @DrawableRes int iconRes, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        this.iconUri = iconUri;
        this.iconRes = iconRes;
        this.tintColor = tintColor;
        this.title = title;
        this.enabled = enabled;
        this.onClickListener = onClickListener;
    }

    public ToolbarButtonItem(String iconUri, String title, boolean enabled, View.OnClickListener onClickListener) {
        this(iconUri, 0, title, 0, enabled, onClickListener);
    }

    public ToolbarButtonItem(@DrawableRes int iconRes, String title, boolean enabled, View.OnClickListener onClickListener) {
        this(null, iconRes, title, 0, enabled, onClickListener);
    }

    public ToolbarButtonItem(String title, View.OnClickListener onClickListener) {
        this(null, 0, title, 0, true, onClickListener);
    }

    public ToolbarButtonItem(@DrawableRes int iconRes, View.OnClickListener onClickListener) {
        this(null, iconRes, null, 0, true, onClickListener);
    }

    private TextView button;

    public void setEnabled(boolean enabled) {
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    public void setTintColor(@ColorInt int tintColor, @ColorInt int backgroundColor) {
        if (button != null) {
            int disableColor = ColorUtils.blendARGB(AppUtils.toGrey(tintColor), backgroundColor, 0.75f);
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_enabled}, // enabled
                    new int[] {-android.R.attr.state_enabled}, // disabled
            };

            int[] colors = new int[] {
                    tintColor,
                    disableColor
            };

            ColorStateList colorStateList = new ColorStateList(states, colors);

            Drawable[] drawables = button.getCompoundDrawables();
            if (drawables[0] != null) {
                DrawableCompat.setTintList(drawables[0], colorStateList);
            }
            button.setTextColor(colorStateList);
        }
    }

    void attach(TextView button) {
        this.button = button;
    }

}
