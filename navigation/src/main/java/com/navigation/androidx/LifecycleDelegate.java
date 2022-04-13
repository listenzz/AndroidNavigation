package com.navigation.androidx;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Listen on 2018/1/31.
 */

public class LifecycleDelegate implements LifecycleObserver {

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
        mLifecycleOwner = lifecycleOwner;
        mHandler = new Handler(Looper.getMainLooper());
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    private static final String TAG = "Navigation";

    private final Queue<Runnable> mTasks = new LinkedList<>();

    private final LifecycleOwner mLifecycleOwner;
    private final Handler mHandler;

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
            mHandler.removeCallbacks(executeTask);
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
            if (runnable != null) {
                runnable.run();
                mHandler.post(executeTask);
            } else {
                mExecuting = false;
            }
        }
    }

    private final Runnable executeTask = new Runnable() {
        @Override
        public void run() {
            mExecuting = false;
            considerExecute();
        }
    };

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
