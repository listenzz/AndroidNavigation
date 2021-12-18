package com.navigation.androidx;

import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.LinkedList;
import java.util.Queue;

public class ImmediateLifecycleDelegate implements LifecycleObserver {

    private final Queue<Runnable> mTasks = new LinkedList<>();

    private final LifecycleOwner mLifecycleOwner;


    public ImmediateLifecycleDelegate(LifecycleOwner lifecycleOwner) {
        mLifecycleOwner = lifecycleOwner;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        if (getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            assertMainThread();
            mTasks.add(runnable);
            considerExecute();
        }
    }

    private boolean mExecuting;

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            mTasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            considerExecute();
        }
    }

    void considerExecute() {
        if (isAtLeastStarted() && !mExecuting) {
            mExecuting = true;
            Runnable runnable = mTasks.poll();
            while (runnable != null) {
                runnable.run();
                runnable = mTasks.poll();
            }
            mExecuting = false;
        }
    }

    boolean isAtLeastStarted() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private Lifecycle getLifecycle() {
        return mLifecycleOwner.getLifecycle();
    }

    private void assertMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("you should perform the task at main thread.");
        }
    }

    static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

}
