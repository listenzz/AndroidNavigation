package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnavigation.R;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;

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
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_home_white_24dp, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.ic_discover_white_24dp, "Discover").setBadgeItem(badgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_discover_white_24dp, "Discover"))
                .setFirstSelectedPosition(0)
                .initialise();



    }

    public List<AwesomeFragment> getFragments() {
        return null;
    }

    public void setFragments(List<AwesomeFragment> fragments) {
        this.fragments = fragments;
    }

    public AwesomeFragment getSelectedFragment() {
        return fragments.get(getSelectedIndex());
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index);
    }

    public void setSelectedIndex(int index) {
        bottomNavigationBar.selectTab(index);
    }

    public int getSelectedIndex() {
        return bottomNavigationBar.getCurrentSelectedPosition();
    }

    public TabBar getTabBar() {
        return null;
    }






}
