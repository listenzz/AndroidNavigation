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

    private final LifecycleDelegate mLifecycleDelegate = new LifecycleDelegate(this);

    private Style mStyle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStyle = new Style(this);
        onCustomStyle(mStyle);
        SystemUI.setDecorFitsSystemWindows(getWindow(), false);
    }

    @Override
    @Nullable
    public Style getStyle() {
        return mStyle;
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            return;
        }

        FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(count - 1);
        AwesomeFragment fragment = (AwesomeFragment) fragmentManager.findFragmentByTag(entry.getName());
        if (fragment == null || !fragment.isAdded()) {
            return;
        }

        if (fragment.dispatchBackPressed()) {
            return;
        }

        if (count != 1) {
            dismissFragment(fragment);
            return;
        }

        if (handleBackPressed()) {
            return;
        }

        ActivityCompat.finishAfterTransition(this);
    }

    protected boolean handleBackPressed() {
        return false;
    }

    @Override
    public void setActivityRootFragment(@NonNull final AwesomeFragment rootFragment) {
        if (isFinishing()) {
            return;
        }

        if (isStateSaved()) {
            scheduleTaskAtStarted(() -> setActivityRootFragmentSync(rootFragment));
            return;
        }
        setActivityRootFragmentSync(rootFragment);
    }

    public boolean isStateSaved() {
        return getSupportFragmentManager().isStateSaved();
    }

    protected void setActivityRootFragmentSync(AwesomeFragment fragment) {
        clearFragmentsSync();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragment.setAnimation(TransitionAnimation.None);
        transaction.add(android.R.id.content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
        getWindow().setBackgroundDrawable(null);
    }

    public void clearFragments() {
        scheduleTaskAtStarted(this::clearFragmentsSync);
    }

    protected void clearFragmentsSync() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count == 0) {
            return;
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(mStyle.getScreenBackgroundColor()));
        String root = fragmentManager.getBackStackEntryAt(0).getName();
        String top = fragmentManager.getBackStackEntryAt(count - 1).getName();
        AwesomeFragment rootFragment = (AwesomeFragment) fragmentManager.findFragmentByTag(root);
        AwesomeFragment topFragment = (AwesomeFragment) fragmentManager.findFragmentByTag(top);

        if (topFragment != null) {
            topFragment.setAnimation(TransitionAnimation.Fade);
            fragmentManager.beginTransaction().setMaxLifecycle(topFragment, Lifecycle.State.STARTED).commit();
        }

        if (rootFragment != null) {
            rootFragment.setAnimation(TransitionAnimation.Fade);
            fragmentManager.popBackStack(root, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void presentFragment(@NonNull AwesomeFragment fragment) {
        presentFragment(fragment, () -> {
        });
    }

    public void presentFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        presentFragment(fragment, completion, TransitionAnimation.Present);
    }

    @Override
    public void presentFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        scheduleTaskAtStarted(() -> presentFragmentSync(fragment, completion, animation));
    }

    private void presentFragmentSync(AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentHelper.handlePresentFragment(fragmentManager, android.R.id.content, fragment, animation);
        completion.run();
    }

    public void dismissFragment(@NonNull AwesomeFragment fragment) {
        if (fragment.getPresentationStyle() == PresentationStyle.OverFullScreen) {
            dismissFragment(fragment, () -> {}, fragment.getAnimation());
        } else {
            dismissFragment(fragment, () -> {
            });
        }
    }

    public void dismissFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion) {
        dismissFragment(fragment, completion, TransitionAnimation.Present);
    }

    @Override
    public void dismissFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        if (fragment.getParentFragmentManager() == getSupportFragmentManager()) {
            scheduleTaskAtStarted(() -> dismissFragmentSync(fragment, completion, animation));
            return;
        }
        fragment.dismissFragment(completion);
    }

    private void dismissFragmentSync(AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation) {
        if (SystemUI.isImeVisible(getWindow())) {
            SystemUI.hideIme(getWindow());
        }

        AwesomeFragment presented = getPresentedFragment(fragment);
        if (presented != null) {
            FragmentHelper.handleDismissFragment(fragment, presented, null, animation);
            completion.run();
            return;
        }

        AwesomeFragment presenting = getPresentingFragment(fragment);
        if (presenting != null) {
            FragmentHelper.handleDismissFragment(presenting, fragment, fragment, animation);
            completion.run();
            return;
        }

        completion.run();
    }

    @Override
    public AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getFragmentAfter(fragment);
    }

    @Override
    public AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getFragmentBefore(fragment);
    }

    public void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode) {
        showAsDialog(dialog, requestCode, () -> {
        });
    }

    public void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode, @NonNull Runnable completion) {
        scheduleTaskAtStarted(() -> showAsDialogSync(dialog, requestCode, completion));
    }

    private void showAsDialogSync(AwesomeFragment dialog, int requestCode, @NonNull Runnable completion) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
        if (fragment != null && fragment.isAdded()) {
            dialog.setTargetFragment(fragment, requestCode);
        }

        Bundle args = FragmentHelper.getArguments(dialog);
        args.putBoolean(AwesomeFragment.ARGS_SHOW_AS_DIALOG, true);
        dialog.setArguments(args);
        fragmentManager
                .beginTransaction()
                .add(dialog, dialog.getSceneId())
                .commit();

        completion.run();
    }

    public void hideAsDialog(@NonNull AwesomeFragment dialog) {
        dialog.hideAsDialog(() -> {
        });
    }

    public void hideAsDialog(@NonNull AwesomeFragment dialog, @NonNull Runnable completion) {
        dialog.hideAsDialog(completion);
    }

    @Nullable
    public AwesomeFragment getDialogFragment() {
        return FragmentHelper.getAwesomeDialogFragment(getSupportFragmentManager());
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        mLifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

}
