package com.navigation.androidx;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Listen on 2017/11/22.
 */

public class AwesomeToolbar extends Toolbar {

    private TextView titleView;
    private TextView leftButton;
    private TextView rightButton;
    private int contentInset;

    private int buttonTintColor;
    private int buttonTextSize;
    private int titleGravity;
    private int titleTextColor;
    private int titleTextSize;

    private List<TextView> leftButtons;
    private List<TextView> rightButtons;

    private ViewOutlineProvider outlineProvider;

    public AwesomeToolbar(Context context) {
        super(context);
        init();
    }

    public AwesomeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AwesomeToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        contentInset = getContentInsetStart();
        setContentInsetStartWithNavigation(getContentInsetStartWithNavigation() - contentInset);
        setContentInsetsRelative(0, 0);
    }

    public void setButtonTintColor(int color) {
        this.buttonTintColor = color;
    }

    @Override
    public void setBackgroundColor(int color) {
        setBackground(new ColorDrawable(color));
        if (color == Color.TRANSPARENT) {
            hideShadow();
        }
    }

    public void setButtonTextSize(int size) {
        buttonTextSize = size;
    }

    @Override
    public void setAlpha(float alpha) {
        Drawable drawable = getBackground();
        drawable.setAlpha((int) (alpha * 255 + 0.5));
        setBackground(drawable);
    }

    public void hideShadow() {
        if (outlineProvider == null) {
            outlineProvider = getOutlineProvider();
        }
        setOutlineProvider(new DefaultOutlineProvider());
    }

    public void showShadow(float elevation) {
        if (outlineProvider != null) {
            setOutlineProvider(outlineProvider);
        }
        setElevation(elevation);
    }

    public void setTitleGravity(int gravity) {
        titleGravity = gravity;
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = titleTextSize;
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleTextSize);
        }
    }

    @Override
    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
        super.setTitleTextColor(titleTextColor);
        if (titleView != null) {
            titleView.setTextColor(titleTextColor);
        }
    }

    public void setAwesomeTitle(int resId) {
        setAwesomeTitle(getContext().getText(resId));
    }

    public void setAwesomeTitle(CharSequence title) {
        TextView titleView = getTitleView();
        titleView.setText(title);
    }

    public TextView getTitleView() {
        if (titleView == null) {
            titleView = new TextView(getContext());
            LayoutParams layoutParams = new LayoutParams(-2, -2, Gravity.CENTER_VERTICAL | titleGravity);
            if (titleGravity == Gravity.START) {
                layoutParams.leftMargin = getContentInset();
            }
            titleView.setMaxLines(1);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setTextColor(titleTextColor);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleTextSize);
            addView(titleView, layoutParams);
        }
        return titleView;
    }

    protected TextView getLeftButton() {
        if (leftButton == null) {
            leftButton = new TextView(getContext());
            leftButton.setGravity(Gravity.CENTER);
            LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER_VERTICAL | Gravity.START);
            layoutParams.leftMargin = AppUtils.dp2px(getContext(), 8);
            addView(leftButton, layoutParams);
        }
        return leftButton;
    }

    protected TextView getRightButton() {
        if (rightButton == null) {
            rightButton = new TextView(getContext());
            rightButton.setGravity(Gravity.CENTER);
            LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, MATCH_PARENT, Gravity.CENTER_VERTICAL | Gravity.END);
            layoutParams.rightMargin = AppUtils.dp2px(getContext(), 8);
            addView(rightButton, layoutParams);
        }
        return rightButton;
    }

    protected int getContentInset() {
        return this.contentInset;
    }

    public void clearLeftButton() {
        if (leftButton != null) {
            removeView(leftButton);
            leftButton = null;
        }
    }

    public void clearLeftButtons() {
        clearLeftButton();
        if (leftButtons != null) {
            for (TextView button : leftButtons) {
                removeView(button);
            }
            leftButtons.clear();
        }
        setNavigationIcon(null);
        setNavigationOnClickListener(null);
    }

    public void clearRightButton() {
        if (rightButton != null) {
            removeView(rightButton);
            rightButton = null;
        }
    }

    public void clearRightButtons() {
        clearRightButton();
        if (rightButtons != null) {
            for (TextView button : rightButtons) {
                removeView(button);
            }
            rightButtons.clear();
        }
        Menu menu = getMenu();
        menu.clear();
    }

    public void addLeftButton(ToolbarButtonItem buttonItem) {
        if (leftButton != null) {
            removeView(leftButton);
        }
        if (leftButtons == null) {
            leftButtons = new ArrayList<>();
        }
        TextView button = new TextView(getContext());
        button.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.START);
        if (leftButtons.size() == 0) {
            layoutParams.leftMargin = AppUtils.dp2px(getContext(), 8);
        }
        addView(button, layoutParams);
        setButton(button, buttonItem);
        leftButtons.add(button);
        bringTitleViewToFront();
    }

    public void addRightButton(ToolbarButtonItem buttonItem) {
        if (rightButton != null) {
            removeView(rightButton);
        }
        if (rightButtons == null) {
            rightButtons = new ArrayList<>();
        }

        TextView button = new TextView(getContext());
        button.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.END);
        if (rightButtons.size() == 0) {
            layoutParams.rightMargin = AppUtils.dp2px(getContext(), 8);
        }
        addView(button, layoutParams);
        setButton(button, buttonItem);
        rightButtons.add(button);
        bringTitleViewToFront();
    }

    public void setLeftButton(ToolbarButtonItem buttonItem) {
        if (leftButtons != null && leftButtons.size() > 0) {
            return;
        }
        setNavigationIcon(null);
        setNavigationOnClickListener(null);
        TextView leftButton = getLeftButton();
        setButton(leftButton, buttonItem);
        bringTitleViewToFront();
    }

    public void setRightButton(ToolbarButtonItem buttonItem) {
        if (rightButtons != null && rightButtons.size() > 0) {
            return;
        }
        TextView rightButton = getRightButton();
        setButton(rightButton, buttonItem);
        bringTitleViewToFront();
    }

    private void bringTitleViewToFront() {
        if (titleView != null) {
            bringChildToFront(titleView);
        }
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        super.setNavigationIcon(icon);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof AppCompatImageButton) {
                AppCompatImageButton button = (AppCompatImageButton) child;
                button.setBackground(null);
            }
        }
    }

    private void setButton(TextView button, ToolbarButtonItem buttonItem) {
        button.setOnClickListener(buttonItem.onClickListener);
        button.setText(null);
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        button.setMaxWidth(Integer.MAX_VALUE);
        button.setVisibility(View.VISIBLE);

        int color = buttonItem.tintColor != 0 ? buttonItem.tintColor : buttonTintColor;
        ColorStateList colorStateList = AppUtils.buttonColorStateList(color);

        Drawable icon = drawableFromBarButtonItem(buttonItem);
        if (icon != null) {
            if (!buttonItem.renderOriginal) {
                DrawableCompat.setTintList(icon, colorStateList);
            }

            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            int size = AppUtils.dp2px(getContext(), 40);
            int padding = (size - icon.getIntrinsicWidth()) / 2;
            button.setWidth(size);
            button.setHeight(size);
            button.setPaddingRelative(padding, 0, padding, 0);

        } else {
            int padding = AppUtils.dp2px(getContext(), 8);
            button.setPaddingRelative(padding, 0, padding, 0);
            button.setText(buttonItem.title);
            button.setTextColor(colorStateList);
            button.setTextSize(buttonTextSize);
            button.setBackground(null);
        }

        button.setEnabled(buttonItem.enabled);
    }

    private Drawable drawableFromBarButtonItem(ToolbarButtonItem barButtonItem) {
        if (getContext() == null) {
            return null;
        }
        Drawable drawable = null;
        if (barButtonItem.iconUri != null) {
            drawable = DrawableUtils.fromUri(getContext(), barButtonItem.iconUri);
        } else if (barButtonItem.iconRes != 0) {
            drawable = ContextCompat.getDrawable(getContext(), barButtonItem.iconRes);
        }
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable.mutate());
        }
        return drawable;
    }

    private static final class DefaultOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {

        }
    }

}
