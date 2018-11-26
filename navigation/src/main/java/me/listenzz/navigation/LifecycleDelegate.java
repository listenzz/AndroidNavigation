package me.listenzz.navigation;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Listen on 2018/1/31.
 */

public class LifecycleDelegate implements LifecycleObserver {

    private Queue<Runnable> tasks = new LinkedList<>();

    private final LifecycleOwner lifecycleOwner;

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            tasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            considerExecute();
        }
    }

    private boolean executing;

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
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
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
