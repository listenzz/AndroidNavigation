package me.listenzz.navigation;

import android.support.annotation.DrawableRes;
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
    public final View.OnClickListener onClickListener;

    public ToolbarButtonItem(String iconUri, String title, boolean enabled, View.OnClickListener onClickListener) {
        this.iconUri = iconUri;
        this.iconRes = 0;
        this.title = title;
        this.enabled = enabled;
        this.onClickListener = onClickListener;
    }

    public ToolbarButtonItem(int iconRes, String title, boolean enabled, View.OnClickListener onClickListener) {
        this.iconUri = null;
        this.iconRes = iconRes;
        this.title = title;
        this.enabled = enabled;
        this.onClickListener = onClickListener;
    }

    TextView button;

    public void setEnabled(boolean enabled) {
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    public void setTextColor(int color) {
        if (button != null) {
            button.setTextColor(color);
        }
    }

    void attach(TextView button) {
        this.button = button;
    }

}
