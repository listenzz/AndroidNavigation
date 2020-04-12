package com.navigation.androidx;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;

/**
 * Created by Listen on 2018/1/30.
 * fork from https://github.com/Ashok-Varma/BottomNavigation/blob/master/bottom-navigation-bar/src/main/java/com/ashokvarma/bottomnavigation/BottomNavigationBar.java
 */

public class TabBar extends FrameLayout {
    ArrayList<TabBarItem> tabBarItems = new ArrayList<>();
    ArrayList<TabView> tabs = new ArrayList<>();

    private Drawable shadowDrawable = new ColorDrawable(Color.parseColor("#dddddd"));
    private static final int DEFAULT_SELECTED_POSITION = -1;
    private int selectedPosition = DEFAULT_SELECTED_POSITION;
    private OnTabSelectedListener tabSelectedListener;

    private int selectedItemColor;
    private int unselectedItemColor;
    private int barBackgroundColor;
    private int badgeColor;

    private FrameLayout container;
    private LinearLayout tabContainer;

    public TabBar(Context context) {
        this(context, null);
    }

    public TabBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TabBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        selectedItemColor = AppUtils.fetchContextColor(context, R.attr.colorAccent);
        unselectedItemColor = Color.LTGRAY;
        barBackgroundColor = Color.WHITE;
        badgeColor = Color.parseColor("#FF3B30");

        setLayoutParams(new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parentView = inflater.inflate(R.layout.nav_tab_bar_container, this, true);
        container = parentView.findViewById(R.id.nav_tab_bar_container);
        tabContainer = parentView.findViewById(R.id.nav_tab_bar_item_container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }

        ViewCompat.setElevation(this, 0);
        setClipToPadding(false);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (shadowDrawable != null) {
            int height = (int) getContext().getResources().getDisplayMetrics().density;
            shadowDrawable.setBounds(0, 0, getWidth(), height);
            shadowDrawable.draw(canvas);
        }
    }

    public void setShadowDrawable(@Nullable Drawable drawable) {
        shadowDrawable = drawable;
        postInvalidate();
    }

    public void updateTabIcon(int index, @NonNull String iconUri, String unselectedIconUri) {
        if (tabBarItems.size() > index) {
            TabBarItem item = tabBarItems.get(index);
            item.iconUri = iconUri;
            item.unselectedIconUri = unselectedIconUri;
        }
    }

    public TabBar addItem(TabBarItem item) {
        tabBarItems.add(item);
        return this;
    }

    public TabBar removeItem(TabBarItem item) {
        tabBarItems.remove(item);
        return this;
    }

    public TabBar setSelectedItemColor(@ColorRes int color) {
        this.selectedItemColor = ContextCompat.getColor(getContext(), color);
        return this;
    }

    public TabBar setSelectedItemColor(@NonNull String color) {
        this.selectedItemColor = Color.parseColor(color);
        return this;
    }

    public TabBar setUnselectedItemColor(@ColorRes int color) {
        this.unselectedItemColor = ContextCompat.getColor(getContext(), color);
        return this;
    }

    public TabBar setUnselectedItemColor(@NonNull String color) {
        this.unselectedItemColor = Color.parseColor(color);
        return this;
    }

    public TabBar setBadgeColor(@ColorRes int color) {
        this.badgeColor = color;
        return this;
    }

    public TabBar setBadgeColor(String color) {
        this.badgeColor = Color.parseColor(color);
        return this;
    }

    public TabBar setBarBackgroundColor(@ColorRes int backgroundColor) {
        this.barBackgroundColor = ContextCompat.getColor(getContext(), backgroundColor);
        return this;
    }

    public TabBar setBarBackgroundColor(String backgroundColorCode) {
        this.barBackgroundColor = Color.parseColor(backgroundColorCode);
        return this;
    }

    public void initialise(int firstSelectedPosition) {
        selectedPosition = DEFAULT_SELECTED_POSITION;
        tabs.clear();

        if (!tabBarItems.isEmpty()) {
            tabContainer.removeAllViews();

            container.setBackgroundColor(barBackgroundColor);

            int screenWidth = AppUtils.getScreenWidth(getContext());
            int itemWidth = getTabWidth(getContext(), screenWidth, tabBarItems.size());

            for (TabBarItem currentItem : tabBarItems) {
                TabView tab = new TabView(getContext());
                setupTab(tab, currentItem, itemWidth);
            }
            if (tabs.size() > firstSelectedPosition) {
                selectTabInternal(firstSelectedPosition, false);
            } else if (!tabs.isEmpty()) {
                selectTabInternal(0, false);
            }
        }
    }

    public TabBar setTabSelectedListener(OnTabSelectedListener tabSelectedListener) {
        this.tabSelectedListener = tabSelectedListener;
        return this;
    }

    public void clearAll() {
        tabContainer.removeAllViews();
        tabs.clear();
        tabBarItems.clear();
        container.setBackgroundColor(Color.TRANSPARENT);
        selectedPosition = DEFAULT_SELECTED_POSITION;
    }

    public void selectTab(int newPosition) {
        selectTab(newPosition, true);
    }

    public void selectTab(int newPosition, boolean callListener) {
        selectTabInternal(newPosition, callListener);
    }

    private void setupTab(TabView tab, TabBarItem currentItem, int itemWidth) {
        tab.setTabWidth(itemWidth);
        tab.setPosition(tabBarItems.indexOf(currentItem));

        tab.setOnClickListener(v -> {
            TabView tabView = (TabView) v;
            selectTabInternal(tabView.getPosition(), true);
        });

        tabs.add(tab);

        bindTabWithData(currentItem, tab, this);

        tab.initialise();

        tabContainer.addView(tab);
    }

    private void selectTabInternal(int newPosition, boolean callListener) {
        int oldPosition = selectedPosition;
        if (selectedPosition != newPosition) {
            if (selectedPosition != DEFAULT_SELECTED_POSITION) {
                tabs.get(selectedPosition).unSelect();
            }
            tabs.get(newPosition).select();
            selectedPosition = newPosition;
        }

        if (callListener) {
            sendListenerCall(oldPosition, newPosition);
        }
    }

    private void sendListenerCall(int oldPosition, int newPosition) {
        if (tabSelectedListener != null) {
            if (oldPosition == newPosition) {
                tabSelectedListener.onTabReselected(newPosition);
            } else {
                tabSelectedListener.onTabSelected(newPosition);
                if (oldPosition != DEFAULT_SELECTED_POSITION) {
                    tabSelectedListener.onTabUnselected(oldPosition);
                }
            }
        }
    }

    public int getCurrentSelectedPosition() {
        return selectedPosition;
    }

    public void showTextBadgeAtIndex(int index, @Nullable String text) {
        TabView tab = tabs.get(index);
        TabBarItem tabBarItem = tabBarItems.get(index);
        tabBarItem.badgeText = text;
        tab.showTextBadge(text);
    }

    public void hideTextBadgeAtIndex(int index) {
        TabBarItem tabBarItem = tabBarItems.get(index);
        tabBarItem.badgeText = null;
        hideBadgeAtIndex(index);
    }

    public void showDotBadgeAtIndex(int index) {
        TabView tab = tabs.get(index);
        TabBarItem tabBarItem = tabBarItems.get(index);
        tabBarItem.showDotBadge = true;
        tab.showDotBadge();
    }

    public void hideDotBadgeAtIndex(int index) {
        TabBarItem tabBarItem = tabBarItems.get(index);
        tabBarItem.showDotBadge = false;
        hideBadgeAtIndex(index);
    }

    public void hideBadgeAtIndex(int index) {
        TabView tab = tabs.get(index);
        TabBarItem tabBarItem = tabBarItems.get(index);
        tabBarItem.showDotBadge = false;
        tabBarItem.badgeText = null;
        tab.hideBadge();
    }

    public interface OnTabSelectedListener {

        void onTabSelected(int position);

        void onTabUnselected(int position);

        void onTabReselected(int position);
    }

    static int getTabWidth(Context context, int screenWidth, int noOfTabs) {
        int maxWidth = (int) context.getResources().getDimension(R.dimen.nav_tab_item_min_width);
        int itemWidth = screenWidth / noOfTabs;
        if (itemWidth > maxWidth) {
            itemWidth = maxWidth;
        }
        return itemWidth;
    }

    static void bindTabWithData(TabBarItem tabBarItem, TabView tab, TabBar tabBar) {

        Context context = tabBar.getContext();
        tab.setLabel(tabBarItem.title);

        tab.setSelectedColor(tabBar.selectedItemColor);
        tab.setUnselectedColor(tabBar.unselectedItemColor);

        if (tabBarItem.iconRes > 0) {
            tab.setIcon(ContextCompat.getDrawable(context, tabBarItem.iconRes));
        } else if (tabBarItem.iconUri != null) {
            tab.setIcon(DrawableUtils.fromUri(context, tabBarItem.iconUri));
        }

        if (tabBarItem.unselectedIconRes > 0) {
            tab.setUnselectedIcon(ContextCompat.getDrawable(context, tabBarItem.unselectedIconRes));
        } else if (tabBarItem.unselectedIconUri != null) {
            tab.setUnselectedIcon(DrawableUtils.fromUri(context, tabBarItem.unselectedIconUri));
        }

        tab.setBadgeColor(tabBar.badgeColor);
        tab.setBadgeText(tabBarItem.badgeText);
        tab.setShowDotBadge(tabBarItem.showDotBadge);
    }
}
