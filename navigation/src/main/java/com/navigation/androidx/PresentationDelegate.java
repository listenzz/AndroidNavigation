package com.navigation.androidx;

import static com.navigation.androidx.AwesomeFragment.ARGS_REQUEST_CODE;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

public class PresentationDelegate {

    private static final String SAVED_STATE_DEFINES_PRESENTATION_CONTEXT = "defines_presentation_context";

    private final AwesomeFragment mFragment;

    private PresentableActivity mPresentableActivity;

    public PresentationDelegate(AwesomeFragment fragment) {
        mFragment = fragment;
    }

    public void setPresentableActivity(PresentableActivity presentableActivity) {
        mPresentableActivity = presentableActivity;
    }

    private boolean mDefinesPresentationContext;

    public boolean definesPresentationContext() {
        return mDefinesPresentationContext;
    }

    public void setDefinesPresentationContext(boolean defines) {
        mDefinesPresentationContext = defines;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mDefinesPresentationContext = savedInstanceState.getBoolean(SAVED_STATE_DEFINES_PRESENTATION_CONTEXT, false);
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SAVED_STATE_DEFINES_PRESENTATION_CONTEXT, mDefinesPresentationContext);
    }

    public void presentFragment(@NonNull final AwesomeFragment presented, final int requestCode, @NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        if (!FragmentHelper.canPresentFragment(mFragment, mFragment.requireActivity())) {
            if (completion != null) {
                completion.run();
            }
            mFragment.onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
            return;
        }

        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext() && presented.getPresentationStyle() == PresentationStyle.CurrentContext) {
                presentFragmentInternal(mFragment, presented, requestCode, completion);
            } else {
                parent.presentFragment(presented, requestCode, animation, completion);
            }
            return;
        }

        if (mPresentableActivity != null) {
            Bundle args = FragmentHelper.getArguments(presented);
            args.putInt(ARGS_REQUEST_CODE, requestCode);
            mPresentableActivity.presentFragment(presented, animation, completion);
        }

    }

    private void presentFragmentInternal(final AwesomeFragment presenting, final AwesomeFragment presented, final int requestCode, @Nullable Runnable completion) {
        Bundle args = FragmentHelper.getArguments(presented);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        presented.setTargetFragment(presenting, requestCode);
        presented.setDefinesPresentationContext(true);
        FragmentHelper.handlePresentFragment(presenting.getParentFragmentManager(), presenting.getContainerId(), presented, TransitionAnimation.Present);
        if (completion != null) {
            completion.run();
        }
    }

    public void dismissFragment(@NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        if (mFragment.getDialogAwesomeFragment() != null) {
            throw new IllegalStateException("在 dialog 中， 不能执行此操作, 如需隐藏 dialog , 请调用 `hideDialog`");
        }

        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext() && mFragment.getPresentationStyle() == PresentationStyle.CurrentContext) {
                dismissFragmentInternal(animation, completion);
            } else {
                parent.dismissFragment(animation, completion);
            }
            return;
        }

        if (mPresentableActivity != null) {
            mPresentableActivity.dismissFragment(mFragment, animation, completion);
        }

    }

    private void dismissFragmentInternal(@NonNull TransitionAnimation animation, @Nullable Runnable completion) {
        AwesomeFragment presented = getPresentedFragment();
        if (presented != null) {
            FragmentHelper.handleDismissFragment(mFragment, presented, null, animation);
            if (completion != null) {
                completion.run();
            }
            return;
        }

        AwesomeFragment presenting = getPresentingFragment();
        if (presenting != null) {
            FragmentHelper.handleDismissFragment(presenting, mFragment, mFragment, animation);
        }

        if (completion != null) {
            completion.run();
        }
    }

    @Nullable
    public AwesomeFragment getPresentedFragment() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                return getPresentedFragmentInternal();
            }
            return parent.getPresentedFragment();
        }

        if (mPresentableActivity != null) {
            return mPresentableActivity.getPresentedFragment(mFragment);
        }

        return null;
    }

    @Nullable
    private AwesomeFragment getPresentedFragmentInternal() {
        if (FragmentHelper.getIndexAtBackStack(mFragment) == -1) {
            return getFragmentFromBackStackAtZeroIndex();
        }
        return FragmentHelper.getFragmentAfter(mFragment);
    }

    @Nullable
    private AwesomeFragment getFragmentFromBackStackAtZeroIndex() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        assert parent != null;
        if (FragmentHelper.getBackStackEntryCount(parent) == 0) {
            return null;
        }
        FragmentManager fragmentManager = mFragment.getParentFragmentManager();
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
        AwesomeFragment presented = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        if (presented == null || !presented.isAdded()) {
            return null;
        }
        return presented;
    }

    @Nullable
    public AwesomeFragment getPresentingFragment() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                return getPresentingFragmentInternal();
            }
            return parent.getPresentingFragment();
        }

        if (mPresentableActivity != null) {
            return mPresentableActivity.getPresentingFragment(mFragment);
        }

        return null;
    }

    @Nullable
    private AwesomeFragment getPresentingFragmentInternal() {
        AwesomeFragment target = (AwesomeFragment) mFragment.getTargetFragment();
        if (target != null && target.isAdded()) {
            return target;
        }
        return null;
    }

}
