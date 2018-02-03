package com.navigation.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class FragmentHelper {

    public static Bundle getArguments(Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
            fragment.setArguments(args);
        }
        return args;
    }

    public static void addFragment(FragmentManager fragmentManager, int containerId, AwesomeFragment fragment, PresentAnimation animation) {
        fragment.setAnimation(animation);
        AwesomeFragment topFragment = (AwesomeFragment) fragmentManager.findFragmentById(containerId);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(containerId, fragment, fragment.getSceneId());
        transaction.setPrimaryNavigationFragment(fragment);

        if (topFragment != null) {
            topFragment.setAnimation(animation);
            transaction.hide(topFragment);
        }

        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();

    }

    public static AwesomeFragment getLatterFragment(FragmentManager fragmentManager, AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > -1 && index < count - 1) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    public static AwesomeFragment getAheadFragment(FragmentManager fragmentManager, AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > 0 && index < count) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    public static int findIndexAtBackStack(FragmentManager fragmentManager, AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = -1;
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            if (fragment.getTag().equals(backStackEntry.getName())) {
                index = i;
            }
        }
        return index;
    }

    public static Fragment findDescendantFragment(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment target = fragmentManager.findFragmentByTag(tag);
        if (target == null) {
            List<Fragment> fragments = fragmentManager.getFragments();
            int count = fragments.size();
            for (int i = count - 1; i > -1; i--) {
                Fragment f = fragments.get(i);
                target = findDescendantFragment(f.getChildFragmentManager(), tag);
                if (target != null) {
                    break;
                }
            }
        }
        return target;
    }


}
