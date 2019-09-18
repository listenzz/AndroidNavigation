package com.navigation;

import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.DrawerFragment;

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
