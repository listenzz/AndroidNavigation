package me.listenzz.navigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Listen on 2017/11/22.
 */

public class AwesomeToolbar extends Toolbar {

    private TextView titleView;
    private TextView leftButton;
    private TextView rightButton;
    private Drawable divider;
    private int dividerAlpha = 255;
    private int contentInset;

    private int backgroundColor;
    private int buttonTintColor;
    private int buttonTextSize;
    private int titleGravity;
    private int titleTextColor;
    private int titleTextSize;

    private List<TextView> leftButtons;
    private List<TextView> rightButtons;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (divider != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int height = (int) getContext().getResources().getDisplayMetrics().density;
            divider.setBounds(0, getHeight() - height, getWidth(), getHeight());
            divider.setAlpha(dividerAlpha);
            divider.draw(canvas);
            divider.setAlpha(255);
        }
    }

    public void setButtonTintColor(int color) {
        this.buttonTintColor = color;
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
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
        if (divider != null) {
            dividerAlpha = (int) (alpha * 255 + 0.5);
            invalidate();
        }
    }

    public void setShadow(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            divider = drawable;
            postInvalidate();
        }
    }

    public void hideShadow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(0);
        } else {
            setShadow(null);
        }
    }

    public void setTitleGravity(int gravity) {
        titleGravity = gravity;
    }

    public void setTitleTextSize(int titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    @Override
    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
        super.setTitleTextColor(titleTextColor);
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
            LayoutParams layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.START);
            addView(leftButton, layoutParams);
        }
        return leftButton;
    }

    protected TextView getRightButton() {
        if (rightButton == null) {
            rightButton = new TextView(getContext());
            rightButton.setGravity(Gravity.CENTER);
            LayoutParams layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.END);
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
        clearLeftButton();
        if (rightButtons != null) {
            for (TextView button : rightButtons) {
                removeView(button);
            }
            rightButtons.clear();
        }
        Menu menu = getMenu();
        menu.clear();
    }

    public void addLeftButton(Drawable icon, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        if (leftButton != null) {
            removeView(leftButton);
        }
        if (leftButtons == null) {
            leftButtons = new ArrayList<>();
        }
        TextView button = new TextView(getContext());
        button.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.START);
        addView(button, layoutParams);
        setButton(button, icon, title, tintColor, enabled, onClickListener);
        leftButtons.add(button);
        bringTitleViewToFront();
    }

    public void addRightButton(Drawable icon, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        if (rightButton != null) {
            removeView(rightButton);
        }
        if (rightButtons == null) {
            rightButtons = new ArrayList<>();
        }

        TextView button = new TextView(getContext());
        button.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.END);
        addView(button, layoutParams);
        setButton(button, icon, title, tintColor, enabled, onClickListener);
        rightButtons.add(button);
        bringTitleViewToFront();
    }

    public void setLeftButton(Drawable icon, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        if (leftButtons != null && leftButtons.size() > 0) {
            return;
        }
        setNavigationIcon(null);
        setNavigationOnClickListener(null);
        TextView leftButton = getLeftButton();
        setButton(leftButton, icon, title, tintColor, enabled, onClickListener);
        bringTitleViewToFront();
    }

    public void setRightButton(Drawable icon, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        if (rightButtons != null && rightButtons.size() > 0) {
            return;
        }
        TextView rightButton = getRightButton();
        setButton(rightButton, icon, title, tintColor, enabled, onClickListener);
        bringTitleViewToFront();
    }

    private void bringTitleViewToFront() {
        if (titleView != null) {
            bringChildToFront(titleView);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                requestLayout();
                invalidate();
            }
        }
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        super.setNavigationIcon(icon);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child instanceof AppCompatImageButton) {
                    AppCompatImageButton button = (AppCompatImageButton) child;
                    button.setBackgroundResource(R.drawable.nav_toolbar_button_item_background);
                }
            }
        }
    }

    private void setButton(TextView button, Drawable icon, String title, int tintColor, boolean enabled, View.OnClickListener onClickListener) {
        button.setOnClickListener(onClickListener);
        button.setText(null);
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        button.setMaxWidth(Integer.MAX_VALUE);
        button.setVisibility(View.VISIBLE);

        int color = tintColor != 0 ? tintColor : buttonTintColor;
        int disableColor = ColorUtils.blendARGB(AppUtils.toGrey(color), backgroundColor, 0.75f);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
        };

        int[] colors = new int[] {
                color,
                disableColor
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);

        if (icon != null) {
            icon = DrawableCompat.wrap(icon);
            DrawableCompat.setTintList(icon, colorStateList);
            button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            int width = getContentInsetStartWithNavigation();
            int padding = (width - icon.getIntrinsicWidth()) / 2;
            button.setMaxWidth(width);
            button.setPaddingRelative(padding, 0, padding, 0);
        } else {
            int padding = getContentInset();
            button.setPaddingRelative(padding, 0, padding, 0);
            button.setText(title);
            button.setTextColor(colorStateList);
            button.setTextSize(buttonTextSize);
        }

        button.setEnabled(enabled);

        TypedValue typedValue = new TypedValue();
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            button.setBackgroundResource(R.drawable.nav_toolbar_button_item_background);
        } else {
            if (getContext().getTheme().resolveAttribute(R.attr.actionBarItemBackground, typedValue, true)) {
                button.setBackgroundResource(typedValue.resourceId);
            }
        }
    }

}
