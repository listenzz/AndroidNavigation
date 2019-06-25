package me.listenzz.navigation;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Queue;

public class ImmediateLifecycleDelegate implements LifecycleObserver {

    private Queue<Runnable> tasks = new LinkedList<>();

    private final LifecycleOwner lifecycleOwner;
    private final Handler handler;

    public ImmediateLifecycleDelegate(LifecycleOwner lifecycleOwner, Handler handler) {
        this.lifecycleOwner = lifecycleOwner;
        this.handler = handler;
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
    private boolean shouldDelay;

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            handler.removeCallbacks(executeTask);
            tasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            shouldDelay = true;
            considerExecute();
        }
    }

    void considerExecute() {
        if (isAtLeastStarted() && !executing) {
            executing = true;
            if (shouldDelay) {
                shouldDelay = false;
                handler.post(executeTask);
            } else {
                Runnable runnable = tasks.poll();
                while (runnable != null) {
                    runnable.run();
                    runnable = tasks.poll();
                }
                executing = false;
            }
        }
    }

    private Runnable executeTask = new Runnable() {
        @Override
        public void run() {
            executing = false;
            considerExecute();
        }
    };

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
