package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidnavigation.R;

/**
 * Created by Listen on 2018/1/11.
 */

public class DrawerFragment extends AwesomeFragment implements DrawerLayout.DrawerListener{

    DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drawer, container, false);
        drawerLayout = root.findViewById(R.id.drawer);
        drawerLayout.addDrawerListener(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        drawerLayout.removeDrawerListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    protected boolean onBackPressed() {
        if (isMenuOpened()) {
            closeMenu();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        setStatusBarHidden(true);
        //setStatusBarStyle("dark-content");
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        setStatusBarHidden(false);
        //setStatusBarStyle("light-content");
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public void setContentFragment(AwesomeFragment fragment) {
        addFragment(R.id.drawer_content, fragment, PresentAnimation.None);
    }

    public AwesomeFragment getContentFragment() {
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_content);
    }

    public void setMenuFragment(AwesomeFragment fragment) {
        addFragment(R.id.drawer_menu, fragment, PresentAnimation.None);
    }

    public AwesomeFragment getMenuFragment() {
        return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.drawer_menu);
    }

    public void openMenu() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(Gravity.START);
            setStatusBarHidden(true);
        }
    }

    public void closeMenu() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(Gravity.START);
            setStatusBarHidden(false);
        }
    }

    public void toggleMenu() {
        if (isMenuOpened()) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    public boolean isMenuOpened() {
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(Gravity.START);
        }
        return false;
    }

}
