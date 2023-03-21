package com.navigation.androidx;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<AwesomeFragment> list = getFragments(fragment.getParentFragmentManager());
        return list.indexOf(fragment);
    }

    @Nullable
    public static AwesomeFragment getFragmentAfter(@NonNull AwesomeFragment fragment) {
        if (!fragment.isAdded()) {
            return null;
        }

        FragmentManager fragmentManager = fragment.getParentFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = getIndexAtBackStack(fragment);
        if (index < 0 || index > count - 2) {
            return null;
        }

        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
        AwesomeFragment successor = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        if (successor != null && successor.isAdded()) {
            return successor;
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
        if (index < 1 || index > count - 1) {
            return null;
        }

        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
        AwesomeFragment precursor = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        if (precursor != null && precursor.isAdded()) {
            return precursor;
        }

        return null;
    }

    public static int getIndexAtBackStack(@NonNull AwesomeFragment fragment) {
        FragmentManager fragmentManager = fragment.getParentFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            String tag = fragment.getTag();
            if (tag != null && tag.equals(backStackEntry.getName())) {
                return i;
            }
        }
        return -1;
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
        if (fragmentManager.isDestroyed()) {
            return null;
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();

        for (int i = count - 1; i > -1; i--) {
            Fragment fragment = fragments.get(i);
            if (!fragment.isAdded()) {
                continue;
            }

            if (tag.equals(fragment.getTag())) {
                return fragment;
            }

            Fragment child = findFragment(fragment.getChildFragmentManager(), tag);
            if (child != null) {
                return child;
            }
        }

        return null;
    }

    @Nullable
    public static Fragment findFragment(@NonNull FragmentManager fragmentManager, @NonNull Class<? extends Fragment> type) {
        if (fragmentManager.isDestroyed()) {
            return null;
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();

        for (int i = count - 1; i > -1; i--) {
            Fragment fragment = fragments.get(i);
            if (!fragment.isAdded()) {
                continue;
            }

            if (fragment.getClass().isAssignableFrom(type)) {
                return fragment;
            }

            Fragment child = findFragment(fragment.getChildFragmentManager(), type);
            if (child != null) {
                return child;
            }
        }

        return null;
    }

    @Nullable
    public static AwesomeFragment getAwesomeDialogFragment(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return null;
        }

        List<Fragment> fragments = fragmentManager.getFragments();
        int count = fragments.size();

        for (int i = count - 1; i > -1; i--) {
            Fragment fragment = fragments.get(i);
            if (!fragment.isAdded()) {
                continue;
            }

            if (fragment instanceof AwesomeFragment && ((AwesomeFragment) fragment).getShowsDialog()) {
                return (AwesomeFragment) fragment;
            }

            AwesomeFragment child = getAwesomeDialogFragment(fragment.getChildFragmentManager());
            if (child != null) {
                return child;
            }
        }

        return null;
    }

    public static void handlePresentFragment(@NonNull FragmentManager fragmentManager, int containerId, @NonNull AwesomeFragment fragment, @NonNull TransitionAnimation animation) {
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
            if (fragment.getPresentationStyle() == PresentationStyle.CurrentContext) {
                transaction.hide(topFragment);
            }
        }
        fragment.setAnimation(animation);

        transaction.add(containerId, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();

        fragmentManager.executePendingTransactions();
    }

    public static void handleDismissFragment(@NonNull AwesomeFragment presenting, @NonNull AwesomeFragment presented, @Nullable AwesomeFragment top, @NonNull TransitionAnimation animation) {
        AwesomeFragment result = getResultFragment(presenting, top);
        result.setAnimation(animation);
        presenting.setAnimation(animation);

        FragmentManager fragmentManager = presenting.getParentFragmentManager();
        fragmentManager.beginTransaction().setMaxLifecycle(presented, Lifecycle.State.STARTED).commit();
        fragmentManager.popBackStack(presented.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.executePendingTransactions();

        handleFragmentResult(presenting, result);
    }

    @NonNull
    private static AwesomeFragment getResultFragment(@NonNull AwesomeFragment presenting, @Nullable AwesomeFragment top) {
        if (top != null) {
            return top;
        }

        FragmentManager fragmentManager = presenting.getParentFragmentManager();
        return (AwesomeFragment) Objects.requireNonNull(fragmentManager.findFragmentById(presenting.getContainerId()));
    }

    public static void handleFragmentResult(AwesomeFragment target, AwesomeFragment resultFragment) {
        final int requestCode = resultFragment.getRequestCode();
        final int resultCode = resultFragment.getResultCode();
        final Bundle data = resultFragment.getResultData();
        if (target.isAdded()) {
            target.onFragmentResult(requestCode, resultCode, data);
        }
    }

    public static boolean isRemoving(@NonNull AwesomeFragment fragment) {
        if (fragment.isRemoving()) {
            return true;
        }

        AwesomeFragment parent = fragment.getParentAwesomeFragment();
        if (parent != null) {
            return isRemoving(parent);
        }

        return false;
    }

    public static boolean isHidden(@NonNull AwesomeFragment fragment) {
        if (fragment.isHidden()) {
            return true;
        }

        AwesomeFragment parent = fragment.getParentAwesomeFragment();
        if (parent != null) {
            return isHidden(parent);
        }

        return false;
    }
}
