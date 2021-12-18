package com.navigation;

import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.DrawerFragment;

public class BaseFragment extends AwesomeFragment {
    @Override
    public void onResume() {
        super.onResume();
        DrawerFragment drawerFragment = getDrawerFragment();
        if (drawerFragment != null) {
            drawerFragment.setMenuInteractive(isStackRoot());
        }
    }
}
