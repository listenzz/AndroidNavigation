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
import com.ashokvarma.bottomnavigation.TextBadgeItem;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarFragment extends AwesomeFragment {

    BottomNavigationBar bottomNavigationBar;

    List<AwesomeFragment> fragments;

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

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);

        TextBadgeItem badgeItem = new TextBadgeItem();
        badgeItem.setText("12");
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
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.ic_discover_white_24dp, "Discover").setBadgeItem(badgeItem))
                .setFirstSelectedPosition(0)
                .initialise();

    }

    public void setFragments(AwesomeFragment... fragments) {
        setFragments(Arrays.asList(fragments));
    }

    public void setFragments(final List<AwesomeFragment> fragments) {
        this.fragments = fragments;
        if (fragments == null || fragments.size() == 0) {
            return;
        }
        scheduleTask(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setReorderingAllowed(true);
                for (int i = 0, size = fragments.size(); i < size; i++) {
                    AwesomeFragment fragment = fragments.get(i);
                    transaction.add(R.id.tabs_content, fragment, fragment.getSceneId());
                    if (i == 0) {
                        transaction.setPrimaryNavigationFragment(fragment);
                    } else {
                        transaction.hide(fragment);
                    }
                }
                transaction.commit();
            }
        });
    }

    public AwesomeFragment getSelectedFragment() {
        return fragments.get(getSelectedIndex());
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index);
    }

    public void setSelectedIndex(final int index) {
        scheduleTask(new Runnable() {
            @Override
            public void run() {
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

    public int getSelectedIndex() {
        return bottomNavigationBar.getCurrentSelectedPosition();
    }


    // -------------------------
    // ------- bottom bar ------
    // -------------------------

    public BottomNavigationBar getBottomNavigationBar() {
        return bottomNavigationBar;
    }

    public void showBottomNavigationBarAnimatedWhenPop(@AnimRes int anim) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(false));
        bottomNavigationBar.startAnimation(animation);
    }

    public void hideBottomNavigationBarAnimatedWhenPush(@AnimRes int anim) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        animation.setAnimationListener(new BottomNavigationBarAnimationListener(true));
        bottomNavigationBar.startAnimation(animation);
    }

    public class BottomNavigationBarAnimationListener implements Animation.AnimationListener {

        boolean hidden;
        public BottomNavigationBarAnimationListener(boolean hidden) {
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
