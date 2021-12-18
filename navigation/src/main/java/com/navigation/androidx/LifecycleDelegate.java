package com.navigation.androidx;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


/**
 * Created by Listen on 2018/1/31.
 */

public class LifecycleDelegate implements LifecycleObserver {

    private final ImmediateLifecycleDelegate mImmediateLifecycleDelegate;
    private final DeferredLifecycleDelegate mDeferredLifecycleDelegate;

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
        mImmediateLifecycleDelegate = new ImmediateLifecycleDelegate(lifecycleOwner);
        mDeferredLifecycleDelegate = new DeferredLifecycleDelegate(lifecycleOwner, new Handler(Looper.getMainLooper()));
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        scheduleTaskAtStarted(runnable, false);
    }

    public void scheduleTaskAtStarted(Runnable runnable, boolean deferred) {
        if (deferred) {
            mDeferredLifecycleDelegate.scheduleTaskAtStarted(runnable);
        } else {
            mImmediateLifecycleDelegate.scheduleTaskAtStarted(runnable);
        }
    }

}
