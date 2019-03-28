package me.listenzz.navigation;

import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;

/**
 * Created by Listen on 2018/1/31.
 */

public class LifecycleDelegate implements LifecycleObserver {

    private final ImmediateLifecycleDelegate immediateLifecycleDelegate;
    private final DeferredLifecycleDelegate deferredLifecycleDelegate;

    public LifecycleDelegate(LifecycleOwner lifecycleOwner) {
        immediateLifecycleDelegate = new ImmediateLifecycleDelegate(lifecycleOwner);
        deferredLifecycleDelegate = new DeferredLifecycleDelegate(lifecycleOwner);
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
