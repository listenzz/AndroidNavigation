package com.navigation.androidx;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.LinkedList;
import java.util.Queue;

public class DeferredLifecycleDelegate implements LifecycleObserver {

    private static final String TAG = "Navigation";

    private static final long INTERVAL = 250;

    private final Queue<Runnable> tasks = new LinkedList<>();

    private final LifecycleOwner lifecycleOwner;
    private final Handler handler;

    public DeferredLifecycleDelegate(LifecycleOwner lifecycleOwner, Handler handler) {
        this.lifecycleOwner = lifecycleOwner;
        this.handler = handler;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        if (getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            assertMainThread();
            handler.post(() -> {
                tasks.add(runnable);
                considerExecute();
            });
        }
    }

    private boolean executing;

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            handler.removeCallbacks(executeTask);
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
            if (runnable != null) {
                runnable.run();
                handler.postDelayed(executeTask, INTERVAL);
            } else {
                executing = false;
            }
        }
    }

    private final Runnable executeTask = new Runnable() {
        @Override
        public void run() {
            executing = false;
            considerExecute();
        }
    };

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
