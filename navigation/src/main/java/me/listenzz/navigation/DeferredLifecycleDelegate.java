package me.listenzz.navigation;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Queue;

public class DeferredLifecycleDelegate implements LifecycleObserver {

    private static final long INTERVAL = 400;

    private Queue<Runnable> tasks = new LinkedList<>();

    private final LifecycleOwner lifecycleOwner;

    private Handler handler = new Handler(Looper.getMainLooper());

    public DeferredLifecycleDelegate(LifecycleOwner lifecycleOwner) {
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
            handler.removeCallbacks(executeTask);
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
            if (runnable != null) {
                runnable.run();
                handler.postDelayed(executeTask, INTERVAL);
            } else {
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
