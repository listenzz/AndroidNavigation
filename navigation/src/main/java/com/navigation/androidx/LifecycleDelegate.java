package com.navigation.androidx;

import android.os.Looper;

import androidx.annotation.UiThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.LinkedList;
import java.util.Queue;

@UiThread
public class LifecycleDelegate implements LifecycleObserver {

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
        mLifecycleOwner = lifecycleOwner;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    private static final String TAG = "Navigation";

    private final Queue<Runnable> mTasks = new LinkedList<>();

    private final LifecycleOwner mLifecycleOwner;

    public void scheduleTaskAtStarted(Runnable runnable) {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        assertMainThread();
        mTasks.add(runnable);
        considerExecute();
    }

    private boolean mExecuting;

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            mTasks.clear();
            getLifecycle().removeObserver(this);
            return;
        }
        considerExecute();
    }

    void considerExecute() {
        if (!isAtLeastStarted()) {
            return;
        }

        if (mExecuting) {
            return;
        }

        Runnable runnable = mTasks.poll();
        while (runnable != null) {
            mExecuting = true;
            runnable.run();
            runnable = mTasks.poll();
        }

        mExecuting = false;
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
