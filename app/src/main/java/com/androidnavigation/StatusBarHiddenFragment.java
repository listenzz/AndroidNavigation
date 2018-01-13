package com.androidnavigation;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarHiddenFragment extends TestStatusBarFragment {

    @Override
    protected boolean prefersStatusBarHidden() {
        return true;
    }
}
