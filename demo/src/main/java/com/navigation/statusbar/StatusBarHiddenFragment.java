package com.navigation.statusbar;

/**
 * Created by listen on 2018/1/13.
 */

public class StatusBarHiddenFragment extends TestStatusBarFragment {

    @Override
    protected boolean preferredStatusBarHidden() {
        return isStatusBarTranslucent();
    }

}
