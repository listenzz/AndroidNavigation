package com.androidnavigation;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidnavigation.fragment.AwesomeFragment;
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
            TestFragment testFragment = new TestFragment();
            presentFragment(testFragment, PresentAnimation.Node);
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
                    dismissFragment(fragment, PresentAnimation.Modal);
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
    public void presentFragment(final AwesomeFragment fragment, final PresentAnimation animation) {
        if (isAtLeastStarted()) {
            executePresentFragment(fragment, animation);
        } else {
            Log.i(TAG, "schedule present");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executePresentFragment(fragment, animation);
                }
            });
        }
    }

    private void executePresentFragment(AwesomeFragment fragment, PresentAnimation animation) {

        fragment.setAnimation(animation);
        FragmentManager fragmentManager = getSupportFragmentManager();
        AwesomeFragment top = (AwesomeFragment) fragmentManager.findFragmentById(android.R.id.content);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, fragment, fragment.getSceneId());
        transaction.setPrimaryNavigationFragment(fragment);

        if (top != null) {
            top.setAnimation(animation);
            transaction.hide(top);
        }

        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    @Override
    public void dismissFragment(final AwesomeFragment fragment, final PresentAnimation animation) {
        if (isAtLeastStarted()) {
            executeDismissFragment(fragment, animation);
        } else {
            Log.i(TAG, "schedule dismiss");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executeDismissFragment(fragment, animation);
                }
            });
        }
    }

    private void executeDismissFragment(AwesomeFragment fragment, PresentAnimation animation) {

        // 如果有 presented 就 dismiss presented, 否则就 dismiss 自己
        AwesomeFragment presented = getPresentedFragment(fragment);
        AwesomeFragment presenting;
        if (presented != null) {
            presenting = fragment;
        } else {
            presented = fragment;
            presenting = getPresentingFragment(fragment);
        }

        presented.setAnimation(animation);
        if (presenting != null) {
            presenting.setAnimation(animation);
        }

        if (presenting == null) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            presenting.onFragmentResult(fragment.getRequestCode(), fragment.getResultCode(), fragment.getResultData());
            getSupportFragmentManager().popBackStack(presented.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    public AwesomeFragment getPresentedFragment(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = findIndexAtBackStack(fragment);
        if (index < count - 2) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index + 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    public AwesomeFragment getPresentingFragment(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int index = findIndexAtBackStack(fragment);
        if (index > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(index - 1);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    public int findIndexAtBackStack(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = -1;
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            if (fragment.getTag().equals(backStackEntry.getName())) {
                index = i;
            }
        }
        return index;
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
