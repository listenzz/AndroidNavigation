package com.navigation.androidx;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Created by listen on 2018/4/5.
 */

public class ToolbarButtonItem {

    public static class Builder {
        private String title;
        private String iconUri;
        private int iconRes;
        private boolean renderOriginal;
        private boolean enabled = true;
        private int tintColor;
        private View.OnClickListener listener;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder icon(@DrawableRes int drawable) {
            this.iconRes = drawable;
            return this;
        }

        public Builder icon(String uri) {
            this.iconUri = uri;
            return this;
        }

        public Builder renderOriginal(boolean original) {
            this.renderOriginal = original;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder tintColor(@ColorInt int color) {
            this.tintColor = color;
            return this;
        }

        public Builder listener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public ToolbarButtonItem build() {
            return new ToolbarButtonItem(iconUri, iconRes, renderOriginal, title, tintColor, enabled, listener);
        }
    }

    public final String title;
    public final String iconUri;
    public final int iconRes;
    public final boolean renderOriginal;
    public final boolean enabled;
    public final int tintColor;
    public final View.OnClickListener onClickListener;

    public ToolbarButtonItem(String iconUri, @DrawableRes int iconRes, boolean renderOriginal, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        this.iconUri = iconUri;
        this.iconRes = iconRes;
        this.renderOriginal = renderOriginal;
        this.tintColor = tintColor;
        this.title = title;
        this.enabled = enabled;
        this.onClickListener = onClickListener;
    }

    private TextView button;

    public void setEnabled(boolean enabled) {
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    public void setTintColor(@ColorInt int tintColor) {
        ColorStateList colorStateList = AppUtils.buttonColorStateList(tintColor);
        if (button != null) {
            if (!renderOriginal) {
                Drawable[] drawables = button.getCompoundDrawables();
                if (drawables[0] != null) {
                    DrawableCompat.setTintList(drawables[0], colorStateList);
                }
            } else {
                button.setTextColor(colorStateList);
            }
        }
    }

    void attach(TextView button) {
        this.button = button;
    }

}
