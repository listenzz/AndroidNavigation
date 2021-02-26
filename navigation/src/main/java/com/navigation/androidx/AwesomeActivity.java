package com.navigation.androidx;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
                    dismissFragment(fragment, null);
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
            scheduleTaskAtStarted(() -> setActivityRootFragmentSync(rootFragment));
        } else {
            if (!isFinishing()) {
                setActivityRootFragmentSync(rootFragment);
            }
        }
    }

    protected void setActivityRootFragmentSync(AwesomeFragment fragment) {
        clearFragmentsSync();
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
            clearFragmentsSync();
            FragmentHelper.executePendingTransactionsSafe(getSupportFragmentManager());
        });
    }

    protected void clearFragmentsSync() {
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

    public void presentFragment(@NonNull AwesomeFragment fragment) {
        presentFragment(fragment, null);
    }

    @Override
    public void presentFragment(@NonNull AwesomeFragment fragment, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> presentFragmentSync(fragment, completion), true);
    }

    private void presentFragmentSync(AwesomeFragment fragment, @Nullable Runnable completion) {
        FragmentHelper.addFragmentToBackStack(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
        if (completion != null) {
            completion.run();
        }
    }

    public void dismissFragment(@NonNull AwesomeFragment fragment) {
        dismissFragment(fragment, null);
    }

    @Override
    public void dismissFragment(@NonNull AwesomeFragment fragment, @Nullable Runnable completion) {
        if (fragment.getFragmentManager() == getSupportFragmentManager()) {
            scheduleTaskAtStarted(() -> dismissFragmentSync(fragment, completion), true);
        } else {
            fragment.dismissFragment(completion);
        }
    }

    private void dismissFragmentSync(AwesomeFragment fragment, @Nullable Runnable completion) {
        AwesomeFragment presented = getPresentedFragment(fragment);
        if (presented != null) {
            FragmentHelper.handleDismissFragment(fragment, presented, null);
            if (completion != null) {
                completion.run();
            }
            return;
        }

        AwesomeFragment presenting = getPresentingFragment(fragment);
        if (presenting != null) {
            FragmentHelper.handleDismissFragment(presenting, fragment, fragment);
        }

        if (completion != null) {
            completion.run();
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

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        showDialog(dialog, requestCode, null);
    }

    public void showDialog(@NonNull AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        scheduleTaskAtStarted(() -> showDialogSync(dialog, requestCode, completion), true);
    }

    public void hideDialog(@NonNull AwesomeFragment dialog) {
        dialog.hideDialog(null);
    }

    public void hideDialog(@NonNull AwesomeFragment dialog, @Nullable Runnable completion) {
        dialog.hideDialog(completion);
    }

    private void showDialogSync(AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
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
        if (completion != null) {
            completion.run();
        }
    }

    @Nullable
    public AwesomeFragment getDialogFragment() {
        return FragmentHelper.getAwesomeDialogFragment(getSupportFragmentManager());
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        scheduleTaskAtStarted(runnable, false);
    }

    public void scheduleTaskAtStarted(Runnable runnable, boolean deferred) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable, deferred);
    }

}
