package me.listenzz.navigation;

import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by Listen on 2018/1/31.
 */

public class LifecycleDelegate implements LifecycleObserver {

    private final ImmediateLifecycleDelegate immediateLifecycleDelegate;
    private final DeferredLifecycleDelegate deferredLifecycleDelegate;
    private Handler handler = new Handler(Looper.getMainLooper());

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
        immediateLifecycleDelegate = new ImmediateLifecycleDelegate(lifecycleOwner, handler);
        deferredLifecycleDelegate = new DeferredLifecycleDelegate(lifecycleOwner, handler);
    }

    public void scheduleTaskAtStarted(Runnable runnable) {
        scheduleTaskAtStarted(runnable, false);
    }

    public void scheduleTaskAtStarted(Runnable runnable, boolean deferred) {
        if (deferred) {
            deferredLifecycleDelegate.scheduleTaskAtStarted(runnable);
        } else {
            immediateLifecycleDelegate.scheduleTaskAtStarted(runnable);
        }
    }

}
