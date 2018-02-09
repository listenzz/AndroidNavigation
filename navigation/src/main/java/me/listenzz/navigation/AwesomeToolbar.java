package me.listenzz.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;


/**
 * Created by Listen on 2017/11/22.
 */

public class AwesomeToolbar extends Toolbar {

    private TextView titleView;

    private TextView leftButton;

    private TextView rightButton;

    private Drawable divider;

    private int contentInset;

    public AwesomeToolbar(Context context) {
        super(context);
        init(context);
    }

    public AwesomeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        leftButton = new TextView(context);
        leftButton.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.START);
        addView(leftButton, layoutParams);

        rightButton = new TextView(context);
        rightButton.setGravity(Gravity.CENTER);
        layoutParams = new LayoutParams(-2, -1, Gravity.CENTER_VERTICAL | Gravity.END);
        addView(rightButton, layoutParams);

        contentInset = getContentInsetStart();
        setContentInsetStartWithNavigation(getContentInsetStartWithNavigation() - contentInset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (divider != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int height = (int) getContext().getResources().getDisplayMetrics().density;
            divider.setBounds(0, getHeight() - height, getWidth(), getHeight());
            divider.draw(canvas);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        if (color == Color.TRANSPARENT) {
            hideShadow();
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
        TextView titleView = getTitleView();
        LayoutParams layoutParams = (LayoutParams) titleView.getLayoutParams();
        layoutParams.gravity = gravity;
        titleView.setLayoutParams(layoutParams);
    }

    @Override
    public void setTitle(int resId) {
        setTitle(getContext().getText(resId));
    }

    @Override
    public void setTitle(CharSequence title) {
        throw new UnsupportedOperationException("please use AwesomeFragment#setTitle to setup title.");
    }

    protected TextView getTitleView() {
        if (titleView == null) {
            titleView = new TextView(getContext());
            LayoutParams layoutParams = new LayoutParams(-2, -2, Gravity.CENTER_VERTICAL | Gravity.START);
            layoutParams.leftMargin = getContentInset();
            addView(titleView, layoutParams);
        }
        return titleView;
    }

    protected TextView getLeftButton() {
        return leftButton;
    }

    protected TextView getRightButton() {
        return rightButton;
    }

    protected int getContentInset() {
        return this.contentInset;
    }
}
