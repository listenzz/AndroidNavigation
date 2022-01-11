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

    public void presentFragment(@NonNull final AwesomeFragment fragment, final int requestCode, @Nullable Runnable completion) {
            if (!FragmentHelper.canPresentFragment(mFragment, mFragment.requireActivity())) {
                if (completion != null) {
                    completion.run();
                }
                mFragment.onFragmentResult(requestCode, Activity.RESULT_CANCELED, null);
                return;
            }

            AwesomeFragment parent = mFragment.getParentAwesomeFragment();
            if (parent != null) {
                if (definesPresentationContext()) {
                    presentFragment(mFragment, fragment, requestCode, completion);
                } else {
                    parent.presentFragment(fragment, requestCode, completion);
                }
                return;
            }

            if (mPresentableActivity != null) {
                Bundle args = FragmentHelper.getArguments(fragment);
                args.putInt(ARGS_REQUEST_CODE, requestCode);
                mPresentableActivity.presentFragment(fragment, completion);
            }

    }

    private void presentFragment(final AwesomeFragment target, final AwesomeFragment fragment, final int requestCode, @Nullable Runnable completion) {
        Bundle args = FragmentHelper.getArguments(fragment);
        args.putInt(ARGS_REQUEST_CODE, requestCode);
        fragment.setTargetFragment(target, requestCode);
        fragment.setDefinesPresentationContext(true);
        FragmentHelper.addFragmentToBackStack(target.getParentFragmentManager(), target.getContainerId(), fragment, TransitionAnimation.Present);
        if (completion != null) {
            completion.run();
        }
    }

    public void dismissFragment(@Nullable Runnable completion) {

            if (mFragment.isInDialog()) {
                throw new IllegalStateException("在 dialog 中， 不能执行此操作, 如需隐藏 dialog , 请调用 `hideDialog`");
            }

            AwesomeFragment parent = mFragment.getParentAwesomeFragment();
            if (parent != null) {
                if (definesPresentationContext()) {
                    AwesomeFragment presented = getPresentedFragment();
                    if (presented != null) {
                        FragmentHelper.handleDismissFragment(mFragment, presented, null);
                        if (completion != null) {
                            completion.run();
                        }
                        return;
                    }

                    AwesomeFragment target = (AwesomeFragment) mFragment.getTargetFragment();
                    if (target != null) {
                        FragmentHelper.handleDismissFragment(target, mFragment, mFragment);
                    }

                    if (completion != null) {
                        completion.run();
                    }
                } else {
                    parent.dismissFragment(completion);
                }
                return;
            }

            if (mPresentableActivity != null) {
                mPresentableActivity.dismissFragment(mFragment, completion);
            }

    }

    @Nullable
    public AwesomeFragment getPresentedFragment() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                FragmentManager fragmentManager = mFragment.getParentFragmentManager();
                if (FragmentHelper.getIndexAtBackStack(mFragment) == -1) {
                    if (FragmentHelper.getBackStackEntryCount(parent) != 0) {
                        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
                        AwesomeFragment presented = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
                        if (presented != null && presented.isAdded()) {
                            return presented;
                        }
                    }
                    return null;
                } else {
                    return FragmentHelper.getFragmentAfter(mFragment);
                }
            } else {
                return parent.getPresentedFragment();
            }
        }

        if (mPresentableActivity != null) {
            return mPresentableActivity.getPresentedFragment(mFragment);
        }

        return null;
    }

    @Nullable
    public AwesomeFragment getPresentingFragment() {
        AwesomeFragment parent = mFragment.getParentAwesomeFragment();
        if (parent != null) {
            if (definesPresentationContext()) {
                AwesomeFragment target = (AwesomeFragment) mFragment.getTargetFragment();
                if (target != null && target.isAdded()) {
                    return target;
                }
                return null;
            } else {
                return parent.getPresentingFragment();
            }
        }

        if (mPresentableActivity != null) {
            return mPresentableActivity.getPresentingFragment(mFragment);
        }

        return null;
    }

}
