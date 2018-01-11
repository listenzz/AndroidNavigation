package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class NavigationFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void pushFragment(AwesomeFragment fragment, boolean animated) {

    }

    public void popFragment(boolean animated) {

    }

    public void popToFragment(AwesomeFragment fragment, boolean animated) {

    }

    public void popToRootFragment(AwesomeFragment fragment, boolean animated) {

    }

    public List<AwesomeFragment> getFragments() {
        return null;
    }

    public void setFragments(List<AwesomeFragment> fragments, boolean animated) {

    }

    public AwesomeFragment getRootFragment() {
        return null;
    }

    public AwesomeFragment getTopFragment() {
        return null;
    }

    public boolean isTopBarHidden() {
        return false;
    }

    public TopBar getTopBar() {
        return null;
    }

}
