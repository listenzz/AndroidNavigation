package com.navigation.androidx;

import static com.navigation.androidx.AwesomeFragment.ARGS_REQUEST_CODE;
import static com.navigation.androidx.AwesomeFragment.ARGS_SHOW_AS_DIALOG;
import static com.navigation.androidx.FragmentHelper.handleFragmentResult;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

public class DialogDelegate {

    private final AwesomeFragment mFragment;

    public DialogDelegate(AwesomeFragment fragment) {
        mFragment = fragment;
    }

    private View getView() {
        return mFragment.getView();
    }

    private Style getStyle() {
        return mFragment.mStyle;
    }

    LayoutInflater onGetLayoutInflater(LayoutInflater layoutInflater, @Nullable Bundle savedInstanceState) {
        Window window = mFragment.getWindow();
        if (window.isFloating()) {
            return layoutInflater;
        }

        return new DialogLayoutInflater(mFragment.requireContext(), layoutInflater,
                () -> {
                    if (mFragment.isCancelable()) {
                        hideAsDialog(() -> {
                        }, false);
                    }
                });
    }

    void onCreate() {
        Bundle args = FragmentHelper.getArguments(mFragment);
        boolean showAsDialog = args.getBoolean(ARGS_SHOW_AS_DIALOG, false);
        mFragment.setShowsDialog(showAsDialog);
    }

    int preferredNavigationBarColor() {
        if (!shouldAnimateDialogTransition()) {
            return Color.TRANSPARENT;
        }

        if (getStyle().getNavigationBarColor() != Style.INVALID_COLOR) {
            return getStyle().getNavigationBarColor();
        }
        return mFragment.requireActivity().getWindow().getNavigationBarColor();

    }

    void setupDialog() {
        Window window = mFragment.getWindow();
        SystemUI.setStatusBarTranslucent(window, true);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Dialog dialog = mFragment.requireDialog();
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_UP || keyCode != KeyEvent.KEYCODE_BACK) {
                return false;
            }
            if (mFragment.dispatchBackPressed()) {
                return true;
            }
            return mFragment.onBackPressed();
        });

        animateInIfNeeded();
    }

    void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode, @NonNull Runnable completion) {
        if (!FragmentHelper.canShowDialog(mFragment, mFragment.requireActivity())) {
            completion.run();
            mFragment.onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
            return;
        }
        showAsDialog(mFragment, dialog, requestCode, completion);
    }

    private void showAsDialog(AwesomeFragment target, AwesomeFragment dialog, int requestCode, @NonNull Runnable completion) {
        Bundle args = FragmentHelper.getArguments(dialog);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        args.putBoolean(ARGS_SHOW_AS_DIALOG, true);
        dialog.setTargetFragment(target, requestCode);
        FragmentManager fragmentManager = target.getParentFragmentManager();
        fragmentManager.beginTransaction()
                .setMaxLifecycle(target, Lifecycle.State.STARTED)
                .add(dialog, dialog.getSceneId())
                .commit();

        completion.run();
    }

    void hideAsDialog(@NonNull Runnable completion, boolean fromAnimation) {
        if (!mFragment.getShowsDialog()) {
            AwesomeFragment dialog = mFragment.getDialogAwesomeFragment();
            if (dialog == null) {
                throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
            }
            dialog.hideAsDialog(completion);
            return;
        }

        AppUtils.hideSoftInput(mFragment.getWindow());

        if (!fromAnimation && animateOutIfNeeded(completion)) {
            return;
        }

        if (!mFragment.isAdded()) {
            return;
        }

        FragmentManager fragmentManager = mFragment.getParentFragmentManager();
        fragmentManager
                .beginTransaction()
                .setMaxLifecycle(mFragment, Lifecycle.State.STARTED)
                .remove(mFragment)
                .commit();

        Fragment target = mFragment.getTargetFragment();
        if (target != null && target.isAdded()) {
            fragmentManager.beginTransaction().setMaxLifecycle(target, Lifecycle.State.RESUMED).commit();
            handleFragmentResult((AwesomeFragment) target, mFragment);
        }

        completion.run();
    }

    void onDismiss() {
        if (!mFragment.isAdded()) {
            return;
        }

        final Fragment target = mFragment.getTargetFragment();
        if (!(target instanceof AwesomeFragment)) {
            return;
        }

        if (!target.isAdded()) {
            return;
        }

        final int requestCode = mFragment.getRequestCode();
        final int resultCode = mFragment.getResultCode();
        final Bundle data = mFragment.getResultData();

        ((AwesomeFragment) target).scheduleTaskAtStarted(() -> {
            FragmentManager fragmentManager = target.getParentFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .setMaxLifecycle(target, Lifecycle.State.RESUMED)
                    .commit();
            AwesomeFragment fragment = (AwesomeFragment) target;
            handleFragmentResult(fragment, requestCode, resultCode, data);
        });
    }

    private void animateInIfNeeded() {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return;
        }

        if (!shouldAnimateDialogTransition()) {
            return;
        }

        DialogFrameLayout frameLayout = (DialogFrameLayout) root;
        View contentView = frameLayout.getChildAt(0);
        animateUpIn(contentView);
    }

    private boolean animateOutIfNeeded(@NonNull Runnable completion) {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return false;
        }

        if (!shouldAnimateDialogTransition()) {
            return false;
        }

        DialogFrameLayout frameLayout = (DialogFrameLayout) root;
        View contentView = frameLayout.getChildAt(0);
        animateDownOut(contentView, completion);
        return true;
    }

    private boolean shouldAnimateDialogTransition() {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return false;
        }
        DialogFrameLayout frameLayout = (DialogFrameLayout) root;
        View contentView = frameLayout.getChildAt(0);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        return layoutParams.gravity == Gravity.BOTTOM;
    }

    private void animateDownOut(@NonNull final View contentView, @NonNull Runnable completion) {
        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f
        );
        AlphaAnimation alpha = new AlphaAnimation(1, 0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(translate);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(200);
        set.setFillAfter(true);
        set.setAnimationListener(createAnimationListener(contentView, completion));
        contentView.startAnimation(set);
    }

    private void animateUpIn(@NonNull final View contentView) {
        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f
        );
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(translate);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(200);
        set.setFillAfter(true);
        contentView.startAnimation(set);
    }

    private Animation.AnimationListener createAnimationListener(@NonNull final View animationView, @NonNull Runnable completion) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationView.post(() -> hideAsDialog(completion, true));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }
}
