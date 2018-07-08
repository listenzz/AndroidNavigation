package com.navigation;

import me.listenzz.navigation.AwesomeFragment;
import me.listenzz.navigation.DrawerFragment;

public class BaseFragment extends AwesomeFragment {

    @Override
    protected void onViewAppear() {
        super.onViewAppear();
        DrawerFragment drawerFragment = getDrawerFragment();
        if (drawerFragment != null) {
            drawerFragment.setMenuInteractive(isNavigationRoot());
        }
    }

}
