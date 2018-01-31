package com.navigation.fragment;

import android.support.annotation.DrawableRes;

/**
 * Created by Listen on 2018/1/15.
 */

public class BarButtonItem {

    public String title;
    public @DrawableRes int icon;

    public BarButtonItem(String title, @DrawableRes int icon) {
        this.title = title;
        this.icon = icon;
    }

}
