package com.androidnavigation.fragment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

/**
 * Created by Listen on 2018/1/11.
 */

public class TopBar extends Toolbar {
    public TopBar(Context context) {
        super(context);
    }

    public TopBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
