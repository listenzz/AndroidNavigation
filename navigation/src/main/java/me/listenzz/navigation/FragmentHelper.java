package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class FragmentHelper {

    private static final String TAG = "Navigation";

    @NonNull
    public static Bundle getArguments(@NonNull Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
            fragment.setArguments(args);
        }
        return args;
    }

    public static void executePendingTransactionsSafe(@NonNull FragmentManager fragmentManager) {
        try {
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            Log.wtf(TAG, e);
        }
    }

    public static void addFragmentToBackStack(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull PresentAnimation animation) {
        if (fragmentManager.isDestroyed()) {
            return;
        }
        executePendingTransactionsSafe(fragmentManager);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        AwesomeFragment topFragment = (AwesomeFragment) fragmentManager.findFragmentById(containerId);
        if (topFragment != null && topFragment.isAdded()) {
            topFragment.setAnimation(animation);
            transaction.hide(topFragment);
        }
        fragment.setAnimation(animation);
        transaction.add(containerId, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public static void addFragmentToAddedList(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment) {
        addFragmentToAddedList(fragmentManager, containerId, fragment, true);
    }

    public static void addFragmentToAddedList(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, boolean primary) {
        if (fragmentManager.isDestroyed()) {
            return;
        }
        executePendingTransactionsSafe(fragmentManager);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, fragment.getSceneId());
        if (primary) {
            transaction.setPrimaryNavigationFragment(fragment); // primary
        }
        transaction.commit();
    }

    @Nullable
    public static AwesomeFragment getLatterFragment(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > -1 && index < count - 1) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    @Nullable
    public static AwesomeFragment getAheadFragment(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > 0 && index < count) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    public static int findIndexAtBackStack(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
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

    @Nullable
    public static Fragment findDescendantFragment(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment target = fragmentManager.findFragmentByTag(tag);
        if (target == null) {
            List<Fragment> fragments = fragmentManager.getFragments();
            int count = fragments.size();
            for (int i = count - 1; i > -1; i--) {
                Fragment f = fragments.get(i);
                if (f instanceof AwesomeFragment) {
                    AwesomeFragment af = (AwesomeFragment) f;
                    if (af.getSceneId().equals(tag)) {
                        target = af;
                    }
                }

                if (target == null) {
                    target = findDescendantFragment(f.getChildFragmentManager(), tag);
                }

                if (target != null) {
                    break;
                }
            }
        }
        return target;
    }

    @Nullable
    public static DialogFragment getDialogFragment(@NonNull FragmentManager fragmentManager) {
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment instanceof DialogFragment) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            if (dialogFragment.getShowsDialog()) {
                return dialogFragment;
            }
        }

        if (fragment != null && fragment.isAdded()) {
            return getDialogFragment(fragment.getChildFragmentManager());
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();
        if (count > 0) {
            fragment = fragments.get(count -1);

            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                if (dialogFragment.getShowsDialog()) {
                    return dialogFragment;
                }
            }

            if (fragment != null && fragment.isAdded()) {
                return getDialogFragment(fragment.getChildFragmentManager());
            }
        }

        return null;
    }

}
