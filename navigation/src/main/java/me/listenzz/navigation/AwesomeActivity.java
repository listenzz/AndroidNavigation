package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class AwesomeActivity extends AppCompatActivity implements PresentableActivity {

    public static final String TAG = "Navigation";

    private static final String SAVED_STATE_STATUS_BAR_TRANSLUCENT = "saved_state_status_bar_translucent";

    private LifecycleDelegate lifecycleDelegate = new LifecycleDelegate(this);

    private Style style;

    private boolean statusBarTranslucent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        style = new Style(this);
        onCustomStyle(style);

        if (savedInstanceState != null) {
            statusBarTranslucent = savedInstanceState.getBoolean(SAVED_STATE_STATUS_BAR_TRANSLUCENT);
            AppUtils.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_STATUS_BAR_TRANSLUCENT, statusBarTranslucent);
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
                    ActivityCompat.finishAfterTransition(this);
                } else {
                    dismissFragment(fragment);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void presentFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executePresentFragment(fragment);
            }
        });
    }

    private void executePresentFragment(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
    }

    @Override
    public void dismissFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeDismissFragment(fragment);
            }
        });
    }

    private void executeDismissFragment(AwesomeFragment fragment) {
        // 如果有 presented 就 dismiss presented, 否则就 dismiss 自己
        AwesomeFragment top = (AwesomeFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        AwesomeFragment presenting = getPresentingFragment(fragment);

        top.setAnimation(PresentAnimation.Modal);

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

    @Override
    public AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getLatterFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment) {
        return FragmentHelper.getAheadFragment(getSupportFragmentManager(), fragment);
    }

    @Override
    public void showDialog(@NonNull final AwesomeFragment dialog, final int requestCode) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeShowDialog(dialog, requestCode);
            }
        });
    }

    private void executeShowDialog(  AwesomeFragment dialog,  int requestCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
            String tag = backStackEntry.getName();
            if (tag != null) {
                Fragment target = fragmentManager.findFragmentByTag(tag);
                dialog.setTargetFragment(target, requestCode);
            }
        }
        dialog.show(fragmentManager, dialog.getSceneId());
    }

    @Override
    @NonNull
    public Style getStyle() {
        return style;
    }

    protected void onCustomStyle(@NonNull Style style) {

    }

    @Override
    public void setActivityRootFragment(@NonNull final AwesomeFragment rootFragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                setRootFragmentInternal(rootFragment);
            }
        });
    }

    private void setRootFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            String tag = fragmentManager.getBackStackEntryAt(0).getName();
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentHelper.addFragmentToBackStack(fragmentManager, android.R.id.content, fragment, PresentAnimation.None);
    }

    @Override
    public void setStatusBarTranslucent(boolean translucent) {
        if (statusBarTranslucent != translucent) {
            statusBarTranslucent = translucent;
            AppUtils.setStatusBarTranslucent(getWindow(), translucent);
            onStatusBarTranslucentChanged(translucent);
        }
    }

    @Override
    public boolean isStatusBarTranslucent() {
        return statusBarTranslucent;
    }

    protected void onStatusBarTranslucentChanged(boolean translucent) {
        List<AwesomeFragment> children = getChildFragmentsAtAddedList();
        for (int i = 0, size = children.size(); i < size; i++) {
            AwesomeFragment child = children.get(i);
            child.onStatusBarTranslucentChanged(translucent);
        }
    }

    protected List<AwesomeFragment> getChildFragmentsAtAddedList() {
        List<AwesomeFragment> children = new ArrayList<>();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            Fragment fragment = fragments.get(i);
            if (fragment instanceof AwesomeFragment) {
                children.add((AwesomeFragment) fragment);
            }
        }
        return children;
    }

    protected void scheduleTaskAtStarted(Runnable runnable) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

}
