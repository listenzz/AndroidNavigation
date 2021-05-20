package com.navigation.androidx;

import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.LinkedList;
import java.util.Queue;

public class ImmediateLifecycleDelegate implements LifecycleObserver {

    private final Queue<Runnable> tasks = new LinkedList<>();

    private final LifecycleOwner lifecycleOwner;


    public ImmediateLifecycleDelegate(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        if (getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            assertMainThread();
            tasks.add(runnable);
            considerExecute();
        }
    }

    private boolean executing;

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            tasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            considerExecute();
        }
    }

    void considerExecute() {
        if (isAtLeastStarted() && !executing) {
            executing = true;
            Runnable runnable = tasks.poll();
            while (runnable != null) {
                runnable.run();
                runnable = tasks.poll();
            }
            executing = false;
        }
    }

    boolean isAtLeastStarted() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private Lifecycle getLifecycle() {
        return lifecycleOwner.getLifecycle();
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
