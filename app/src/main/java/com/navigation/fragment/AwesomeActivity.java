package com.navigation.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

public abstract class AwesomeActivity extends AppCompatActivity implements PresentableActivity, FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "Navigation";

    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);

    private Style style = new Style();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        onCustomStyle(style);
        // Log.i(TAG, "onCreate");
        setStatusBarTranslucent(true);
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(count - 1);
            AwesomeFragment fragment = (AwesomeFragment) fragmentManager.findFragmentByTag(entry.getName());
            if (!fragment.dispatchBackPressed()) {
                if (count == 1) {
                    Log.w(TAG, "finish activity");
                    ActivityCompat.finishAfterTransition(this);
                } else {
                    Log.i(TAG, "dismiss:");
                    dismissFragment(fragment);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, getClass().getSimpleName() + " Entry index:" + entry.getId() + " tag:" + entry.getName());
        }
    }

    @Override
    public void presentFragment(final AwesomeFragment fragment) {
        if (isAtLeastStarted()) {
            executePresentFragment(fragment);
        } else {
            Log.i(TAG, "schedule present");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executePresentFragment(fragment);
                }
            });
        }
    }

    @Override
    public void dismissFragment(final AwesomeFragment fragment) {
        if (isAtLeastStarted()) {
            executeDismissFragment(fragment);
        } else {
            Log.i(TAG, "schedule dismiss");
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    executeDismissFragment(fragment);
                }
            });
        }
    }

    @Override
    public AwesomeFragment getPresentedFragment(AwesomeFragment fragment) {
        return FragmentHelper.getLatterFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public AwesomeFragment getPresentingFragment(AwesomeFragment fragment) {
        return FragmentHelper.getAheadFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public Style getStyle() {
        return style;
    }

    protected void onCustomStyle(Style style) {

    }

    public void setRootFragment(final AwesomeFragment fragment) {
        Fragment f = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (f != null) {
            scheduleTask(new Runnable() {
                @Override
                public void run() {
                    setRootFragmentInternal(fragment);
                }
            });
        } else {
            setRootFragmentInternal(fragment);
        }
    }

    private void setRootFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            AwesomeFragment top = (AwesomeFragment) fragmentManager.findFragmentByTag(fragmentManager.getBackStackEntryAt(count - 1).getName());
            top.getNavigationFragment().setAnimation(PresentAnimation.None);

            String tag = fragmentManager.getBackStackEntryAt(0).getName();
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragment.setAnimation(PresentAnimation.None);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    private void setStatusBarTranslucent(boolean translucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                        return defaultInsets.replaceSystemWindowInsets(
                                defaultInsets.getSystemWindowInsetLeft(),
                                0,
                                defaultInsets.getSystemWindowInsetRight(),
                                defaultInsets.getSystemWindowInsetBottom());
                    }
                });
            } else {
                decorView.setOnApplyWindowInsetsListener(null);
            }

            ViewCompat.requestApplyInsets(decorView);
        }
    }

    public int getStatusBarHeight() {
        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight1;
    }

    private void executePresentFragment(AwesomeFragment fragment) {
        FragmentHelper.addFragment(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
    }

    private void executeDismissFragment(AwesomeFragment fragment) {
        // 如果有 presented 就 dismiss presented, 否则就 dismiss 自己
        AwesomeFragment top = (AwesomeFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        AwesomeFragment presenting = getPresentingFragment(fragment);


        top.setAnimation(PresentAnimation.Modal);
        top.getInnermostFragment().setAnimation(PresentAnimation.Modal);

        if (presenting != null) {
            presenting.setAnimation(PresentAnimation.Modal);
        }

        if (presenting == null) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            presenting.onFragmentResult(fragment.getRequestCode(), fragment.getResultCode(), fragment.getResultData());
            getSupportFragmentManager().popBackStack(fragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    protected void scheduleTask(Runnable runnable) {
        lifecycleDelegate.scheduleTask(runnable);
    }

    protected boolean isAtLeastStarted() {
        return lifecycleDelegate.isAtLeastStarted();
    }

    protected boolean isAtLeastCreated() {
        return lifecycleDelegate.isAtLeatCreated();
    }

}
