package com.navigation.androidx;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

public abstract class AwesomeActivity extends AppCompatActivity implements PresentableActivity {

    public static final String TAG = "Navigation";

    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);

    private Style style;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        style = new Style(this);
        onCustomStyle(style);
        SystemUI.setStatusBarTranslucent(getWindow(), true);
    }

    @Override
    @Nullable
    public Style getStyle() {
        return style;
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment fragment = (AwesomeFragment) fragmentManager.findFragmentByTag(entry.getName());
            if (fragment != null && fragment.isAdded() && !fragment.dispatchBackPressed()) {
                if (count == 1) {
                    if (!handleBackPressed()) {
                        ActivityCompat.finishAfterTransition(this);
                    }
                } else {
                    dismissFragment(fragment);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    protected boolean handleBackPressed() {
        return false;
    }


    @Override
    public void setActivityRootFragment(@NonNull final AwesomeFragment rootFragment) {
        if (getSupportFragmentManager().isStateSaved()) {
            scheduleTaskAtStarted(() -> setRootFragmentInternal(rootFragment));
        } else {
            if (!isFinishing()) {
                setRootFragmentInternal(rootFragment);
            }
        }
    }

    protected void setRootFragmentInternal(AwesomeFragment fragment) {
        clearFragmentsInternal();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragment.setAnimation(PresentAnimation.None);
        transaction.add(android.R.id.content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
    }

    public void clearFragments() {
        scheduleTaskAtStarted(() -> {
            clearFragmentsInternal();
            FragmentHelper.executePendingTransactionsSafe(getSupportFragmentManager());
        });
    }

    protected void clearFragmentsInternal() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            getWindow().setBackgroundDrawable(new ColorDrawable(style.getScreenBackgroundColor()));
            String root = fragmentManager.getBackStackEntryAt(0).getName();
            String top = fragmentManager.getBackStackEntryAt(count - 1).getName();
            AwesomeFragment rootFragment = (AwesomeFragment) fragmentManager.findFragmentByTag(root);
            AwesomeFragment topFragment = (AwesomeFragment) fragmentManager.findFragmentByTag(top);
            if (topFragment != null) {
                topFragment.setAnimation(PresentAnimation.Fade);
                fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();
            }
            if (rootFragment != null) {
                rootFragment.setAnimation(PresentAnimation.Fade);
                fragmentManager.popBackStack(root, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public void presentFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> presentFragmentInternal(fragment), true);
    }

    private void presentFragmentInternal(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
    }

    @Override
    public void dismissFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(() -> dismissFragmentInternal(fragment), true);
    }

    protected void dismissFragmentInternal(AwesomeFragment fragment) {
        AwesomeFragment presented = getPresentedFragment(fragment);
        if (presented != null) {
            FragmentHelper.handleDismissFragment(fragment, presented, null);
            return;
        }
        AwesomeFragment presenting = getPresentingFragment(fragment);
        if (presenting != null) {
            FragmentHelper.handleDismissFragment(presenting, fragment, fragment);
        }
    }

    @Override
    public AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getFragmentAfter(fragment);
    }

    @Override
    public AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getFragmentBefore(fragment);
    }

    public void showDialog(@NonNull final AwesomeFragment dialog, final int requestCode) {
        scheduleTaskAtStarted(() -> showDialogInternal(dialog, requestCode), true);
    }

    protected void showDialogInternal(AwesomeFragment dialog, int requestCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
            if (fragment != null && fragment.isAdded()) {
                dialog.setTargetFragment(fragment, requestCode);
            }
        }
        Bundle args = FragmentHelper.getArguments(dialog);
        args.putBoolean(AwesomeFragment.ARGS_SHOW_AS_DIALOG, true);
        dialog.show(fragmentManager, dialog.getSceneId());
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
    }

    @Nullable
    public DialogFragment getDialogFragment() {
        return FragmentHelper.getDialogFragment(getSupportFragmentManager());
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        scheduleTaskAtStarted(runnable, false);
    }

    public void scheduleTaskAtStarted(Runnable runnable, boolean deferred) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable, deferred);
    }

}
