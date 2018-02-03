package com.navigation.library;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.navigation.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarFragment extends AwesomeFragment {

    private static final String SAVED_FRAGMENT_TAGS = "fragment_tags";
    private static final String SAVED_POSITION = "position";
    private static final String SAVED_BOTTOM_BAR_HIDDEN = "bottom_bar_hidden";

    private BottomBar bottomBar;

    private List<AwesomeFragment> fragments;

    private ArrayList<String> fragmentTags = new ArrayList<>();
    private ArrayList<TextBadgeItem> badges = new ArrayList<>();

    private int position;
    private boolean bottomBarHidden;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabbar, container, false);
        bottomBar = root.findViewById(R.id.bottom_bar);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // fragments
        if (savedInstanceState != null) {
            fragmentTags = savedInstanceState.getStringArrayList(SAVED_FRAGMENT_TAGS);
            fragments = new ArrayList<>();
            FragmentManager fragmentManager = getChildFragmentManager();
            for (int i = 0, size = fragmentTags.size(); i < size; i++) {
                fragments.add((AwesomeFragment) fragmentManager.findFragmentByTag(fragmentTags.get(i)));
            }
            initBottomNavigationBar(fragments);
        } else {
            if (fragments != null) {
                addFragments(fragments);
                initBottomNavigationBar(fragments);
            }
        }

        // bottomBar
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(SAVED_POSITION);
            bottomBar.selectTab(position);
            bottomBarHidden = savedInstanceState.getBoolean(SAVED_BOTTOM_BAR_HIDDEN, false);
            if (bottomBarHidden) {
                bottomBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVED_FRAGMENT_TAGS, fragmentTags);
        outState.putInt(SAVED_POSITION, position);
        outState.putBoolean(SAVED_BOTTOM_BAR_HIDDEN, bottomBarHidden);
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getSelectedFragment();
    }

    public void setFragments(AwesomeFragment... fragments) {
        setFragments(Arrays.asList(fragments));
    }

    public void setFragments(List<AwesomeFragment> fragments) {
        if (isAtLeastCreated()) {
            addFragments(fragments);
            initBottomNavigationBar(fragments);
        }
        this.fragments = fragments;
    }

    private void addFragments(List<AwesomeFragment> fragments) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        for (int i = 0, size = fragments.size(); i < size; i++) {
            AwesomeFragment fragment = fragments.get(i);
            fragmentTags.add(fragment.getSceneId());
            transaction.add(R.id.tabs_content, fragment, fragment.getSceneId());
            if (i == 0) {
                transaction.setPrimaryNavigationFragment(fragment);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    private void initBottomNavigationBar(List<AwesomeFragment> fragments) {
        bottomBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                Log.i(TAG, "tab position:" + position);
                setSelectedIndex(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });

        for (int i = 0, size = fragments.size(); i < size; i++) {
            AwesomeFragment fragment = fragments.get(i);
            TabBarItem tabBarItem = fragment.getTabBarItem();
            BottomNavigationItem bottomNavigationItem;
            if (tabBarItem != null) {
                Drawable icon = new ColorDrawable();
                if (tabBarItem.iconRes != -1) {
                    icon = ContextCompat.getDrawable(getContext(), tabBarItem.iconRes);
                } else if (tabBarItem.iconUri != null) {
                    icon = DrawableUtils.fromUri(getContext(), tabBarItem.iconUri);
                }
                bottomNavigationItem = new BottomNavigationItem(icon, tabBarItem.title);
            } else {
                bottomNavigationItem = new BottomNavigationItem(new ColorDrawable(), "Tab");
            }

            TextBadgeItem textBadgeItem = new TextBadgeItem();
            textBadgeItem.setBackgroundColor("#FF3B30");
            bottomNavigationItem.setBadgeItem(textBadgeItem);
            bottomBar.addItem(bottomNavigationItem);
            badges.add(textBadgeItem);
        }

        onBottomBarInitialise(bottomBar);
        bottomBar.initialise();

        for (int i = 0, size = badges.size(); i < size; i++) {
            TextBadgeItem badgeItem = badges.get(i);
            badgeItem.hide(false);
        }
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index);
    }

    public AwesomeFragment getSelectedFragment() {
        return fragments.get(getSelectedIndex());
    }

    public int getSelectedIndex() {
        return bottomBar.getCurrentSelectedPosition();
    }

    public void setSelectedIndex(final int index) {
        bottomBar.selectTab(index, false);
        scheduleTask(new Runnable() {
            @Override
            public void run() {
                position = index;
                FragmentManager fragmentManager = getChildFragmentManager();
                Fragment previous = fragmentManager.getPrimaryNavigationFragment();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(previous);
                AwesomeFragment current = fragments.get(index);
                transaction.setPrimaryNavigationFragment(current);
                transaction.show(current);
                transaction.commit();

                if (current.getNavigationFragment() != null && current.shouldHideBottomBarWhenPushed()) {
                    if (current.getChildFragmentCountAtBackStack() <= 1) {
                        bottomBar.setVisibility(View.VISIBLE);
                    } else {
                        bottomBar.setVisibility(View.GONE);
                    }
                } else {
                    bottomBar.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    // -------------------------
    // ------- bottom bar ------
    // -------------------------

    public void toggleBottomBar() {
        bottomBar.toggle();
    }

    protected void onBottomBarInitialise(BottomBar bottomBar) {
        bottomBar.setBarBackgroundColor(style.getBottomBarBackgroundColor());

        if (style.getBottomBarActiveColor() != null) {
            bottomBar.setActiveColor(style.getBottomBarActiveColor());
        }

        if (style.getBottomBarInActiveColor() != null) {
            bottomBar.setInActiveColor(style.getBottomBarInActiveColor());
        }

        bottomBar.setShadow(style.getBottomBarShadow());
    }

    public void setBadge(final int index, final String text) {
        scheduleTask(new Runnable() {
            @Override
            public void run() {
                TextBadgeItem textBadgeItem = badges.get(index);
                if (TextUtils.isEmpty(text)) {
                    textBadgeItem.hide();
                } else {
                    textBadgeItem.setText(text);
                    textBadgeItem.show();
                }
            }
        });
    }

    protected BottomNavigationBar getBottomBar() {
        return bottomBar;
    }

    void showBottomNavigationBarAnimatedWhenPop(@AnimRes int anim) {
        bottomBarHidden = false;
       // Log.w(TAG, "bottomBarHidden:" + bottomBarHidden);
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(false));
        bottomBar.startAnimation(animation);
    }

    void hideBottomNavigationBarAnimatedWhenPush(@AnimRes int anim) {
        bottomBarHidden = true;
       // Log.w(TAG, "bottomBarHidden:" + bottomBarHidden);
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(true));
        bottomBar.startAnimation(animation);
    }

    class BottomNavigationBarAnimationListener implements Animation.AnimationListener {

        boolean hidden;

        BottomNavigationBarAnimationListener(boolean hidden) {
            this.hidden = hidden;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (hidden) {
                bottomBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!hidden) {
                bottomBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


}
