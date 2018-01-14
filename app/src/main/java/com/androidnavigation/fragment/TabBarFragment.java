package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.androidnavigation.R;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

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

    BottomNavigationBar bottomNavigationBar;

    List<AwesomeFragment> fragments;

    ArrayList<String> fragmentTags = new ArrayList<>();

    int position;

    boolean bottomBarHidden;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabbar, container, false);
        bottomNavigationBar = root.findViewById(R.id.bottom_bar);
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
        } else {
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

        // bottomNavigationBar

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
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
            bottomNavigationBar.addItem(new BottomNavigationItem(tabBarItem.icon, tabBarItem.title));
        }

        bottomNavigationBar
                .initialise();

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(SAVED_POSITION);
            bottomNavigationBar.selectTab(position);
            bottomBarHidden = savedInstanceState.getBoolean(SAVED_BOTTOM_BAR_HIDDEN, false);
            if (bottomBarHidden) {
                bottomNavigationBar.setVisibility(View.GONE);
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
    public boolean isContainer() {
        return true;
    }


    @Override
    protected AwesomeFragment childFragmentForStatusBarColor() {
        return getSelectedFragment();
    }

    @Override
    protected AwesomeFragment childFragmentForStatusBarStyle() {
        return getSelectedFragment();
    }

    @Override
    protected AwesomeFragment childFragmentForStatusBarHidden() {
        return getSelectedFragment();
    }

    public void setFragments(AwesomeFragment... fragments) {
        setFragments(Arrays.asList(fragments));
    }

    public void setFragments(final List<AwesomeFragment> fragments) {
        this.fragments = fragments;
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index);
    }

    public AwesomeFragment getSelectedFragment() {
        return fragments.get(getSelectedIndex());
    }

    public int getSelectedIndex() {
        return bottomNavigationBar.getCurrentSelectedPosition();
    }

    public void setSelectedIndex(final int index) {

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
            }
        });
    }

    // -------------------------
    // ------- bottom bar ------
    // -------------------------

    public void toggleBottomBar() {
        bottomNavigationBar.toggle();
    }

    protected BottomNavigationBar getBottomNavigationBar() {
        return bottomNavigationBar;
    }

    void showBottomNavigationBarAnimatedWhenPop(@AnimRes int anim) {
        bottomBarHidden = false;
        Log.w(TAG, "bottomBarHidden:" + bottomBarHidden);
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(false));
        bottomNavigationBar.startAnimation(animation);
    }

    void hideBottomNavigationBarAnimatedWhenPush(@AnimRes int anim) {
        bottomBarHidden = true;
        Log.w(TAG, "bottomBarHidden:" + bottomBarHidden);
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(true));
        bottomNavigationBar.startAnimation(animation);
    }

    class BottomNavigationBarAnimationListener implements Animation.AnimationListener {

        boolean hidden;

        BottomNavigationBarAnimationListener(boolean hidden) {
            this.hidden = hidden;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (hidden) {
                bottomNavigationBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!hidden) {
                bottomNavigationBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


}
