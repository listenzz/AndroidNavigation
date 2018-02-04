package com.navigation.library;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public abstract class AwesomeActivity extends AppCompatActivity implements PresentableActivity, FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "Navigation";

    private static final String SAVED_STATE_CONTENT_UNDER_STATUS_BAR = "saved_state_content_under_status_bar";

    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);

    private Style style;

    private boolean contentUnderStatusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        style = new Style(this);
        onCustomStyle(style);

        if (savedInstanceState != null) {
            contentUnderStatusBar = savedInstanceState.getBoolean(SAVED_STATE_CONTENT_UNDER_STATUS_BAR);
            AppUtils.setStatusBarTranslucent(getWindow(), contentUnderStatusBar);
        }

        //Log.i(TAG, "onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_CONTENT_UNDER_STATUS_BAR, contentUnderStatusBar);
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment fragment = (AwesomeFragment) fragmentManager.findFragmentByTag(entry.getName());
            if (!fragment.dispatchBackPressed()) {
                if (count == 1) {
                    // Log.w(TAG, "finish activity");
                    ActivityCompat.finishAfterTransition(this);
                } else {
                    // Log.i(TAG, "dismiss:");
                    dismissFragment(fragment);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, getClass().getSimpleName() + " Entry index:" + entry.getId() + " tag:" + entry.getName());
        }
    }

    @Override
    public void presentFragment(final AwesomeFragment fragment) {
        if (isAtLeastStarted()) {
            executePresentFragment(fragment);
        } else {
            Log.i(TAG, "schedule present");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executePresentFragment(fragment);
                }
            });
        }
    }

    @Override
    public void dismissFragment(final AwesomeFragment fragment) {
        if (isAtLeastStarted()) {
            executeDismissFragment(fragment);
        } else {
            Log.i(TAG, "schedule dismiss");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executeDismissFragment(fragment);
                }
            });
        }
    }

    @Override
    public AwesomeFragment getPresentedFragment(AwesomeFragment fragment) {
        return FragmentHelper.getLatterFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public AwesomeFragment getPresentingFragment(AwesomeFragment fragment) {
        return FragmentHelper.getAheadFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public Style getStyle() {
        return style;
    }

    protected void onCustomStyle(Style style) {

    }

    public void setRootFragment(final AwesomeFragment fragment) {
        Fragment f = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (f != null) {
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    setRootFragmentInternal(fragment);
                }
            });
        } else {
            setRootFragmentInternal(fragment);
        }
    }

    private void setRootFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            AwesomeFragment top = (AwesomeFragment) fragmentManager.findFragmentByTag(fragmentManager.getBackStackEntryAt(count - 1).getName());
            top.getNavigationFragment().setAnimation(PresentAnimation.None);

            String tag = fragmentManager.getBackStackEntryAt(0).getName();
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragment.setAnimation(PresentAnimation.None);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    @Override
    public void setContentUnderStatusBar(boolean under) {
        if (contentUnderStatusBar != under) {
            contentUnderStatusBar = under;
            AppUtils.setStatusBarTranslucent(getWindow(), under);
            onContentUnderStatusBar(under);
        }
    }

    @Override
    public boolean isContentUnderStatusBar() {
        return contentUnderStatusBar;
    }

    protected void onContentUnderStatusBar(boolean under) {
        List<AwesomeFragment> children = getAddedChildFragments();
        for (int i = 0, size = children.size(); i < size; i ++) {
            AwesomeFragment child = children.get(i);
            child.onContentUnderStatusBar(under);
        }
    }

    protected List<AwesomeFragment> getAddedChildFragments() {
        List<AwesomeFragment> children = new ArrayList<>();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            children.add((AwesomeFragment) fragments.get(i));
        }
        return children;
    }

    private void executePresentFragment(AwesomeFragment fragment) {
        FragmentHelper.addFragment(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
    }

    private void executeDismissFragment(AwesomeFragment fragment) {
        // 如果有 presented 就 dismiss presented, 否则就 dismiss 自己
        AwesomeFragment top = (AwesomeFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        AwesomeFragment presenting = getPresentingFragment(fragment);


        top.setAnimation(PresentAnimation.Modal);
        top.getInnermostFragment().setAnimation(PresentAnimation.Modal);

        if (presenting != null) {
            presenting.setAnimation(PresentAnimation.Modal);
        }

        if (presenting == null) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            presenting.onFragmentResult(fragment.getRequestCode(), fragment.getResultCode(), fragment.getResultData());
            getSupportFragmentManager().popBackStack(fragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    protected void scheduleTask(Runnable runnable) {
        lifecycleDelegate.scheduleTask(runnable);
    }

    protected boolean isAtLeastStarted() {
        return lifecycleDelegate.isAtLeastStarted();
    }

    protected boolean isAtLeastCreated() {
        return lifecycleDelegate.isAtLeastCreated();
    }

}
