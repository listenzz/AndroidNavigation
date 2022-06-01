package com.navigation.androidx;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

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

    public static void addFragmentToBackStack(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull TransitionAnimation animation) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        AwesomeFragment topFragment = (AwesomeFragment) fragmentManager.findFragmentById(containerId);
        if (topFragment != null && topFragment.isAdded()) {
            topFragment.setAnimation(animation);
            transaction.setMaxLifecycle(topFragment, Lifecycle.State.STARTED);
            transaction.hide(topFragment);
        }
        fragment.setAnimation(animation);

        transaction.add(containerId, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public static void addFragment(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull Lifecycle.State maxLifecycle) {
        addFragment(fragmentManager, containerId, fragment, maxLifecycle, true);
    }

    public static void addFragment(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull Lifecycle.State maxLifecycle, boolean primary) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, fragment.getSceneId());
        if (primary) {
            transaction.setPrimaryNavigationFragment(fragment); // primary
        }
        transaction.setMaxLifecycle(fragment, maxLifecycle);
        transaction.commit();
    }

    @NonNull
    public static List<AwesomeFragment> getFragments(@NonNull FragmentManager fragmentManager) {
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

    public static int indexOf(@NonNull AwesomeFragment fragment) {
        List<AwesomeFragment> list = getFragments(fragment.requireFragmentManager());
        return list.indexOf(fragment);
    }

    @Nullable
    public static AwesomeFragment getFragmentAfter(@NonNull AwesomeFragment fragment) {
        if (!fragment.isAdded()) {
            return null;
        }
        FragmentManager fragmentManager = fragment.requireFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = getIndexAtBackStack(fragment);
        if (index > -1 && index < count - 1) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
            AwesomeFragment next = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            if (next != null && next.isAdded()) {
                return next;
            }
        }
        return null;
    }

    @Nullable
    public static AwesomeFragment getFragmentBefore(@NonNull AwesomeFragment fragment) {
        if (!fragment.isAdded()) {
            return null;
        }
        FragmentManager fragmentManager = fragment.getParentFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = getIndexAtBackStack(fragment);
        if (index > 0 && index < count) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
            AwesomeFragment previous = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            if (previous != null && previous.isAdded()) {
                return previous;
            }
        }
        return null;
    }

    public static int getIndexAtBackStack(@NonNull AwesomeFragment fragment) {
        FragmentManager fragmentManager = fragment.getParentFragmentManager();
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

    public static int getBackStackEntryCount(@NonNull AwesomeFragment fragment) {
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        return fragmentManager.getBackStackEntryCount();
    }

    @Nullable
    public static AwesomeFragment findAwesomeFragment(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment fragment = findFragment(fragmentManager, tag);
        if (fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    @Nullable
    public static AwesomeFragment findAwesomeFragment(@NonNull FragmentManager fragmentManager, @NonNull Class<? extends Fragment> type) {
        Fragment fragment = findFragment(fragmentManager, type);
        if (fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    @Nullable
    public static Fragment findFragment(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment target = fragmentManager.findFragmentByTag(tag);
        if (target == null) {
            List<Fragment> fragments = fragmentManager.getFragments();
            int count = fragments.size();
            for (int i = count - 1; i > -1; i--) {
                Fragment fragment = fragments.get(i);
                if (fragment.isAdded()) {
                    if (tag.equals(fragment.getTag())) {
                        target = fragment;
                    }

                    if (target == null) {
                        target = findFragment(fragment.getChildFragmentManager(), tag);
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
    public static Fragment findFragment(@NonNull FragmentManager fragmentManager, @NonNull Class<? extends Fragment> type) {
        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();
        Fragment target = null;
        for (int i = count - 1; i > -1; i--) {
            Fragment fragment = fragments.get(i);
            if (fragment.isAdded()) {
                if (type.isAssignableFrom(fragment.getClass())) {
                    target = fragment;
                }

                if (target == null) {
                    target = findFragment(fragment.getChildFragmentManager(), type);
                }

                if (target != null) {
                    break;
                }
            }
        }

        return target;
    }

    @Nullable
    public static AwesomeFragment getAwesomeDialogFragment(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return null;
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();

        AwesomeFragment dialog = null;

        for (int i = count - 1; i > -1; i--) {
            Fragment fragment = fragments.get(i);
            if (fragment instanceof AwesomeFragment && fragment.isAdded()) {
                AwesomeFragment dialogFragment = (AwesomeFragment) fragment;
                if (dialogFragment.getShowsDialog()) {
                    dialog = dialogFragment;
                }

                if (dialog == null) {
                    dialog = getAwesomeDialogFragment(fragment.getChildFragmentManager());
                }

                if (dialog != null) {
                    break;
                }
            }
        }

        return dialog;
    }

    public static void handleDismissFragment(@NonNull AwesomeFragment target, @NonNull AwesomeFragment presented, @Nullable AwesomeFragment top, @NonNull TransitionAnimation animation) {
        FragmentManager fragmentManager = target.getParentFragmentManager();
        target.setAnimation(animation);

        if (top == null) {
            top = (AwesomeFragment) fragmentManager.findFragmentById(target.getContainerId());
        }

        if (top == null) {
            return;
        }

        top.setAnimation(animation);
        fragmentManager.beginTransaction().setMaxLifecycle(presented, Lifecycle.State.STARTED).commit();
        fragmentManager.popBackStack(presented.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

        handleFragmentResult(target, top);
    }

    public static void handleFragmentResult(AwesomeFragment target, AwesomeFragment resultFragment) {
        final int requestCode = resultFragment.getRequestCode();
        final int resultCode = resultFragment.getResultCode();
        final Bundle data = resultFragment.getResultData();
        handleFragmentResult(target, requestCode, resultCode, data);
    }

    public static void handleFragmentResult(AwesomeFragment target, int requestCode, int resultCode, Bundle data) {
        View view = target.getView();
        if (view != null) {
            view.post(() -> {
                if (target.isAdded()) {
                    target.onFragmentResult(requestCode, resultCode, data);
                }
            });
        }
    }

    public static boolean isRemoving(@NonNull AwesomeFragment fragment) {
        while (fragment != null) {
            if (fragment.isRemoving()) {
                return true;
            }
            fragment = fragment.getParentAwesomeFragment();
        }
        return false;
    }

    public static boolean isHidden(@NonNull AwesomeFragment fragment) {
        while (fragment != null) {
            if (fragment.isHidden()) {
                return true;
            }
            fragment = fragment.getParentAwesomeFragment();
        }
        return false;
    }

    public static boolean canPresentFragment(@NonNull AwesomeFragment fragment, @NonNull FragmentActivity activity) {
        AwesomeFragment presented = fragment.getPresentedFragment();
        if (presented != null) {
            Log.w(TAG, "Can't present since the fragment had present another fragment already.");
            return false;
        }

        DialogFragment dialog = getAwesomeDialogFragment(activity.getSupportFragmentManager());
        if (dialog != null) {
            Log.w(TAG, "Can't present a fragment over a dialog.");
            return false;
        }

        return true;
    }

    public static boolean canShowDialog(@NonNull AwesomeFragment fragment, @NonNull FragmentActivity activity) {
        AwesomeFragment presented = fragment.getPresentedFragment();
        if (presented != null) {
            Log.w(TAG, "Can't show dialog since the fragment had present another fragment already.");
            return false;
        }

        DialogFragment dialog = getAwesomeDialogFragment(activity.getSupportFragmentManager());
        if (dialog != null && dialog != fragment) {
            Log.w(TAG, "Can't show dialog since there are another dialog over there.");
            return false;
        }
        return true;
    }

}
