package com.androidnavigation;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidnavigation.fragment.AwesomeFragment;
import com.androidnavigation.fragment.FragmentHelper;
import com.androidnavigation.fragment.PresentAnimation;
import com.androidnavigation.fragment.PresentableActivity;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements PresentableActivity, LifecycleObserver, FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "AndroidNavigation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null) {
            FragmentHelper.addFragment(getSupportFragmentManager(), android.R.id.content, new TestNavigationFragment(), PresentAnimation.None);
        }
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
                    ActivityCompat.finishAfterTransition(this);
                } else {
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

    private void executePresentFragment(AwesomeFragment fragment) {
        FragmentHelper.addFragment(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
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

    private void executeDismissFragment(AwesomeFragment fragment) {
        // 如果有 presented 就 dismiss presented, 否则就 dismiss 自己
        AwesomeFragment presented = getPresentedFragment(fragment);
        AwesomeFragment presenting;
        if (presented != null) {
            presenting = fragment;
        } else {
            presented = fragment;
            presenting = getPresentingFragment(fragment);
        }

        presented.setAnimation(PresentAnimation.Modal);
        if (presenting != null) {
            presenting.setAnimation(PresentAnimation.Modal);
        }

        if (presenting == null) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            presenting.onFragmentResult(fragment.getRequestCode(), fragment.getResultCode(), fragment.getResultData());
            getSupportFragmentManager().popBackStack(presented.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    public AwesomeFragment getPresentedFragment(AwesomeFragment fragment) {
        return FragmentHelper.getLatterFragment(getSupportFragmentManager(), fragment);
    }

    public AwesomeFragment getPresentingFragment(AwesomeFragment fragment) {
        return FragmentHelper.getAheadFragment(getSupportFragmentManager(), fragment);
    }

    private boolean active;
    private LinkedList<Runnable> tasks = new LinkedList<>();

    protected void scheduleTask(Runnable runnable) {
        if (getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            tasks.add(runnable);
            considerExecute();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            // 清空队列
            tasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            activeStateChanged(isActiveState(getLifecycle().getCurrentState()));
        }
    }

    void activeStateChanged(boolean newActive) {
        if (newActive != this.active) {
            this.active = newActive;
            considerExecute();
        }
    }

    void considerExecute() {
        if (active) {
            if (isActiveState(getLifecycle().getCurrentState())) {
                if (tasks.size() > 0) {
                    for (Runnable task : tasks) {
                        task.run();
                    }
                    tasks.clear();
                }
            }
        }
    }

    boolean isActiveState(Lifecycle.State state) {
        return state.isAtLeast(Lifecycle.State.STARTED);
    }

    boolean isAtLeastStarted() {
        return isActiveState(getLifecycle().getCurrentState());
    }
}
