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

    void attach(TextView button) {
        this.button = button;
    }

}
