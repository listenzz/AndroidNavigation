package com.navigation.androidx;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Created by Listen on 2019/7/12.
 * fork from https://github.com/Ashok-Varma/BottomNavigation/blob/master/bottom-navigation-bar/src/main/java/com/ashokvarma/bottomnavigation/BottomNavigationTab.java
 */
public class TabView extends FrameLayout {

    protected int position;
    protected int selectedColor;
    protected int unselectedColor;
    protected int badgeColor;
    protected String badgeText;
    protected boolean showDotBadge;

    protected Drawable icon;
    protected Drawable unselectedIcon;
    protected String label;

    boolean selected = false;

    View containerView;
    TextView labelView;
    ImageView iconView;
    FrameLayout iconContainerView;
    TextView badgeView;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.nav_tab_bar_item, this, true);
        containerView = view.findViewById(R.id.nav_tab_item_container);
        labelView = view.findViewById(R.id.nav_tab_item_title);
        iconView = view.findViewById(R.id.nav_tab_item_icon);
        iconContainerView = view.findViewById(R.id.nav_tab_item_icon_container);
        badgeView = view.findViewById(R.id.nav_tab_item_badge);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setTabWidth(int activeWidth) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = activeWidth;
        setLayoutParams(params);
    }

    public void setIcon(Drawable icon) {
        this.icon = DrawableCompat.wrap(icon);
    }

    public void setUnselectedIcon(@Nullable Drawable icon) {
        unselectedIcon = null;
        if (icon != null) {
            unselectedIcon = DrawableCompat.wrap(icon);
        }
    }

    public void setLabel(String label) {
        this.label = label;
        labelView.setText(label);
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public void setShowDotBadge(boolean showDotBadge) {
        this.showDotBadge = showDotBadge;
    }

    public void setSelectedColor(int color) {
        selectedColor = color;
    }

    public void setUnselectedColor(int color) {
        unselectedColor = color;
        labelView.setTextColor(color);
    }

    public void setBadgeColor(int badgeColor) {
        this.badgeColor = badgeColor;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void showTextBadge(String text) {
        if (!TextUtils.isEmpty(text)) {
            FrameLayout.LayoutParams layoutParams = (LayoutParams) badgeView.getLayoutParams();
            layoutParams.topMargin = AppUtils.dp2px(getContext(), 4);
            layoutParams.rightMargin = AppUtils.dp2px(getContext(), 0);
            badgeView.setMinWidth(AppUtils.dp2px(getContext(), 18));
            badgeView.setHeight(AppUtils.dp2px(getContext(), 18));
            if (text != null && text.length() > 1) {
                int padding = AppUtils.dp2px(getContext(), 6);
                badgeView.setPadding(padding, 0, padding, 0);
            } else {
                badgeView.setPadding(0, 0, 0, 0);
            }
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.nav_text_badge_background);
            assert drawable != null;
            DrawableCompat.setTint(drawable, badgeColor);
            badgeView.setBackground(drawable);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            badgeView.setText(text);
            badgeView.setVisibility(VISIBLE);
        } else {
            hideBadge();
        }
    }

    public void showDotBadge() {
        FrameLayout.LayoutParams layoutParams = (LayoutParams) badgeView.getLayoutParams();
        layoutParams.topMargin = AppUtils.dp2px(getContext(), 8);
        layoutParams.rightMargin = AppUtils.dp2px(getContext(), 8);
        badgeView.setMinWidth(AppUtils.dp2px(getContext(), 8));
        badgeView.setHeight(AppUtils.dp2px(getContext(), 8));
        badgeView.setPadding(0, 0, 0, 0);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.nav_dot_badge_background);
        assert drawable != null;
        DrawableCompat.setTint(drawable, badgeColor);
        badgeView.setBackground(drawable);
        badgeView.setText(null);
        badgeView.setVisibility(VISIBLE);
    }

    public void hideBadge() {
        badgeView.setVisibility(View.GONE);
    }

    public void select() {
        selected = true;
        iconView.setSelected(true);
        labelView.setTextColor(selectedColor);
    }

    public void unSelect() {
        selected = false;
        labelView.setTextColor(unselectedColor);
        iconView.setSelected(false);
    }

    @CallSuper
    public void initialise() {
        iconView.setSelected(false);
        if (icon != null && unselectedIcon != null) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected},
                    icon);
            states.addState(new int[]{-android.R.attr.state_selected},
                    unselectedIcon);
            states.addState(new int[]{},
                    unselectedIcon);
            iconView.setImageDrawable(states);
        } else if (icon != null) {
            DrawableCompat.setTintList(icon, new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //1
                            new int[]{-android.R.attr.state_selected}, //2
                            new int[]{}
                    },
                    new int[]{
                            selectedColor, //1
                            unselectedColor, //2
                            unselectedColor //3
                    }
            ));
            iconView.setImageDrawable(icon);
        }

        if (showDotBadge) {
            showDotBadge();
        } else if (!TextUtils.isEmpty(badgeText)) {
            showTextBadge(badgeText);
        } else {
            hideBadge();
        }

        if (selected) {
            select();
        }
    }
}
