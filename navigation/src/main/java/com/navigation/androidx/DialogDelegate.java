package com.navigation.androidx;

import static com.navigation.androidx.AwesomeFragment.ARGS_REQUEST_CODE;
import static com.navigation.androidx.AwesomeFragment.ARGS_SHOW_AS_DIALOG;

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

    LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = mFragment.requireActivity().getLayoutInflater();
        Window window = mFragment.getWindow();
        boolean isFloating = window != null && window.isFloating();
        if (mFragment.getShowsDialog() && !isFloating) {
            layoutInflater = new DialogLayoutInflater(mFragment.requireContext(), layoutInflater,
                    () -> {
                        if (mFragment.isCancelable()) {
                            hideAsDialog(null, false);
                        }
                    });
        }

        return layoutInflater;
    }

    int preferredNavigationBarColor() {
        if (shouldAnimateDialogTransition()) {
            if (getStyle().getNavigationBarColor() != Style.INVALID_COLOR) {
                return getStyle().getNavigationBarColor();
            } else {
                return mFragment.requireActivity().getWindow().getNavigationBarColor();
            }
        } else {
            return Color.TRANSPARENT;
        }
    }

    void setupDialog() {
        Window window = mFragment.getWindow();

        if (window != null) {
            SystemUI.setStatusBarTranslucent(window, true);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        Dialog dialog = mFragment.getDialog();
        if (dialog != null) {
            dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (!mFragment.dispatchBackPressed() && mFragment.isCancelable()) {
                        hideAsDialog(null, false);
                    }
                    return true;
                }
                return false;
            });
        }

        animateInIfNeeded();
    }

    void showAsDialog(@NonNull AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        if (!FragmentHelper.canShowDialog(mFragment, mFragment.requireActivity())) {
            if (completion != null) {
                completion.run();
            }
            mFragment.onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
            return;
        }
        showAsDialog(mFragment, dialog, requestCode, completion);
    }

    private void showAsDialog(AwesomeFragment target, AwesomeFragment dialog, int requestCode, @Nullable Runnable completion) {
        Bundle args = FragmentHelper.getArguments(dialog);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        args.putBoolean(ARGS_SHOW_AS_DIALOG, true);
        dialog.setTargetFragment(target, requestCode);
        FragmentManager fragmentManager = target.getParentFragmentManager();
        fragmentManager.beginTransaction()
                .setMaxLifecycle(target, Lifecycle.State.STARTED)
                .add(dialog, dialog.getSceneId())
                .commit();

        if (completion != null) {
            completion.run();
        }
    }

    void hideAsDialog(@Nullable Runnable completion, boolean fromAnimation) {
        if (!mFragment.isInDialog()) {
            throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
        }

        if (!mFragment.getShowsDialog()) {
            AwesomeFragment parent = mFragment.getParentAwesomeFragment();
            if (parent != null) {
                parent.hideAsDialog(completion);
            } else {
                throw new IllegalStateException("Can't find a dialog, do you mean `dismissFragment`?");
            }
            return;
        }

        AppUtils.hideSoftInput(mFragment.getWindow());

        if (!fromAnimation && animateOutIfNeeded(completion)) {
            return;
        }

        if (mFragment.isAdded()) {
            FragmentManager fragmentManager = mFragment.getParentFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .setMaxLifecycle(mFragment, Lifecycle.State.STARTED)
                    .remove(mFragment)
                    .commit();
            Fragment target = mFragment.getTargetFragment();
            if (target instanceof AwesomeFragment && target.isAdded()) {
                fragmentManager.beginTransaction().setMaxLifecycle(target, Lifecycle.State.RESUMED).commit();
                fragmentManager.executePendingTransactions();
                AwesomeFragment fragment = (AwesomeFragment) target;
                fragment.onFragmentResult(mFragment.getRequestCode(), mFragment.getResultCode(), mFragment.getResultData());
            }
        }

        if (completion != null) {
            completion.run();
        }
    }

    void onDismiss() {
        if (mFragment.isAdded()) {
            Fragment target = mFragment.getTargetFragment();
            if (target instanceof AwesomeFragment && target.isAdded()) {
                ((AwesomeFragment) target).scheduleTaskAtStarted(() -> {
                    FragmentManager fragmentManager = mFragment.getParentFragmentManager();
                    fragmentManager.beginTransaction().setMaxLifecycle(target, Lifecycle.State.RESUMED).commit();
                    fragmentManager.executePendingTransactions();
                    AwesomeFragment fragment = (AwesomeFragment) target;
                    fragment.onFragmentResult(mFragment.getRequestCode(), mFragment.getResultCode(), mFragment.getResultData());
                });
            }
        }
    }

    private void animateInIfNeeded() {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return;
        }

        if (shouldAnimateDialogTransition()) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            animateUpIn(contentView);
        }
    }

    private boolean animateOutIfNeeded(@Nullable Runnable completion) {
        View root = getView();
        if (!(root instanceof DialogFrameLayout)) {
            return false;
        }

        if (shouldAnimateDialogTransition()) {
            DialogFrameLayout frameLayout = (DialogFrameLayout) root;
            View contentView = frameLayout.getChildAt(0);
            animateDownOut(contentView, completion);
            return true;
        }

        return false;
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

    private void animateDownOut(@NonNull final View contentView, @Nullable Runnable completion) {
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

    private Animation.AnimationListener createAnimationListener(@NonNull final View animationView, @Nullable Runnable completion) {
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
