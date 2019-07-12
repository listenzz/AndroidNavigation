package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.ArrayList;
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
            Log.w(TAG, "executePendingTransactionsSafe:" + fragmentManager);
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            Log.wtf(TAG, e);
        }
    }

    public static void addFragmentToBackStack(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull PresentAnimation animation) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

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
        executePendingTransactionsSafe(fragmentManager);
    }

    public static void addFragmentToAddedList(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment) {
        addFragmentToAddedList(fragmentManager, containerId, fragment, true);
    }

    public static void addFragmentToAddedList(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, boolean primary) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, fragment.getSceneId());
        if (primary) {
            transaction.setPrimaryNavigationFragment(fragment); // primary
        }
        transaction.commit();
        executePendingTransactionsSafe(fragmentManager);
    }

    @Nullable
    public static AwesomeFragment getLatterFragment(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > -1 && index < count - 1) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
            AwesomeFragment latter = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            if (latter != null && latter.isAdded()) {
                return latter;
            }
        }
        return null;
    }

    @Nullable
    public static AwesomeFragment getAheadFragment(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragmentManager, fragment);
        if (index > 0 && index < count) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
            AwesomeFragment ahead = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            if (ahead != null && ahead.isAdded()) {
                return ahead;
            }
        }
        return null;
    }

    public static int findIndexAtBackStack(@NonNull FragmentManager fragmentManager, @NonNull AwesomeFragment fragment) {
        int count = fragmentManager.getBackStackEntryCount();
        int index = -1;
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            String tag = fragment.getTag();
            if (tag != null && tag.equals(backStackEntry.getName())) {
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
                if (f.isAdded()) {
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
        }
        return target;
    }

    @Nullable
    public static DialogFragment getDialogFragment(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return null;
        }

        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null && fragment.isAdded()) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                if (dialogFragment.getShowsDialog()) {
                    return dialogFragment;
                }
            }
            return getDialogFragment(fragment.getChildFragmentManager());
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();

        for (int i = count - 1; i > -1; i--) {
            fragment = fragments.get(i);
            if (fragment.isAdded()) {
                if (fragment instanceof DialogFragment) {
                    DialogFragment dialogFragment = (DialogFragment) fragment;
                    if (dialogFragment.getShowsDialog()) {
                        return dialogFragment;
                    }
                }
                return getDialogFragment(fragment.getChildFragmentManager());
            }
        }

        return null;
    }

    @NonNull
    public static List<AwesomeFragment> getFragmentsAtAddedList(@NonNull FragmentManager fragmentManager) {
        List<AwesomeFragment> children = new ArrayList<>();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            Fragment fragment = fragments.get(i);
            if (fragment instanceof AwesomeFragment && fragment.isAdded()) {
                children.add((AwesomeFragment) fragment);
            }
        }
        return children;
    }

    public static void handleDismissFragment(@NonNull AwesomeFragment target, @NonNull AwesomeFragment presented, @Nullable AwesomeFragment top) {
        FragmentManager fragmentManager = target.requireFragmentManager();
        target.setAnimation(PresentAnimation.Modal);

        if (top == null) {
            top = (AwesomeFragment) fragmentManager.findFragmentById(target.getContainerId());
        }

        if (top == null) {
            return;
        }

        top.setAnimation(PresentAnimation.Modal);
        top.setUserVisibleHint(false);
        fragmentManager.popBackStack(presented.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
        target.onFragmentResult(top.getRequestCode(), top.getResultCode(), top.getResultData());
    }

    public static boolean canPresentFragment(@NonNull AwesomeFragment fragment, @NonNull FragmentActivity activity) {
        AwesomeFragment presented = fragment.getPresentedFragment();
        if (presented != null) {
            Log.w(TAG, "can not present since the fragment had present another fragment already.");
            return false;
        }

        DialogFragment dialog = getDialogFragment(activity.getSupportFragmentManager());
        if (dialog != null) {
            Log.w(TAG, "can not present a fragment over a dialog.");
            return false;
        }

        return true;
    }

    public static boolean canShowDialog(@NonNull AwesomeFragment fragment, @NonNull FragmentActivity activity) {
        AwesomeFragment presented = fragment.getPresentedFragment();
        if (presented != null) {
            Log.w(TAG, "can not show dialog since the fragment had present another fragment already.");
            return false;
        }

        DialogFragment dialog = getDialogFragment(activity.getSupportFragmentManager());
        if (dialog != null && dialog != fragment) {
            Log.w(TAG, "can not show dialog since the fragment had show another dialog already.");
            return false;
        }
        return true;
    }

}
