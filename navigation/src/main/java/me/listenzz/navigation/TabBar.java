package me.listenzz.navigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Listen on 2018/1/30.
 */

public class TabBar extends BottomNavigationBar {

    private Drawable shadow = new ColorDrawable(Color.parseColor("#dddddd"));
    private List<ImageView> imageViews = new ArrayList<>();
    private List<TextView> badgeViews = new ArrayList<>();

    public TabBar(Context context) {
        super(context);
    }

    public TabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TabBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (shadow != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int height = (int) getContext().getResources().getDisplayMetrics().density;
            shadow.setBounds(0, 0, getWidth(), height);
            shadow.draw(canvas);
        }
    }

    public void setShadow(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            shadow = drawable;
            postInvalidate();
        }
    }

    @Override
    public void initialise() {
        super.initialise();
        LinearLayout itemContainer = findViewById(R.id.bottom_navigation_bar_item_container);
        int count = itemContainer.getChildCount();
        imageViews.clear();
        badgeViews.clear();
        for (int i = 0; i < count; i++) {
            View itemLayout = itemContainer.getChildAt(i);
            ImageView iconView = itemLayout.findViewById(R.id.fixed_bottom_navigation_icon);
            iconView.setScaleType(ImageView.ScaleType.CENTER);
            imageViews.add(iconView);

            TextView textView = itemLayout.findViewById(R.id.fixed_bottom_navigation_badge);
            badgeViews.add(textView);
        }
    }

    public ImageView imageViewAtTab(int index) {
        return imageViews.get(index);
    }

    // only call this method after #initialise
    public void setTabIcon(int index, Drawable drawable, Drawable inactiveDrawable) {
        ImageView imageView = imageViewAtTab(index);
        if (inactiveDrawable != null) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected},
                    drawable);
            states.addState(new int[]{-android.R.attr.state_selected},
                    inactiveDrawable);
            states.addState(new int[]{},
                    inactiveDrawable);
            imageView.setImageDrawable(states);
        } else {
            DrawableCompat.setTintList(drawable, new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //1
                            new int[]{-android.R.attr.state_selected}, //2
                            new int[]{}
                    },
                    new int[]{
                            getActiveColor(), //1
                            getInActiveColor(), //2
                            getInActiveColor() //3
                    }
            ));
            imageView.setImageDrawable(drawable);
        }
    }

    public TextView badgeViewAtTab(int index) {
        return badgeViews.get(index);
    }

    public void setBadge(int index, String text) {
        TextView badgeView = badgeViewAtTab(index);
        if (TextUtils.isEmpty(text)) {
            badgeView.setVisibility(GONE);
        } else {
            badgeView.setText(text);
            badgeView.setScaleX(1);
            badgeView.setScaleY(1);
            badgeView.setTextColor(Color.WHITE);
            badgeView.setVisibility(VISIBLE);
        }
    }

    public void setRedPoint(int index, boolean visible) {
        TextView badgeView = badgeViewAtTab(index);
        if (!visible) {
            badgeView.setVisibility(GONE);
        } else {
            badgeView.setText("1");
            badgeView.setTextColor(Color.parseColor("#FF3B30"));
            badgeView.setScaleX(0.5f);
            badgeView.setScaleY(0.5f);
            badgeView.setVisibility(VISIBLE);
        }
    }
}
