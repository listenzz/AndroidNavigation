package me.listenzz.navigation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

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
            if (fragment != null && !fragment.dispatchBackPressed()) {
                if (count == 1) {
                    if (!handleBackPressed()) {
                        ActivityCompat.finishAfterTransition(this);
                    }
                } else {
                    dismissFragment(fragment);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    protected boolean handleBackPressed() {
        return false;
    }

    @Override
    public void presentFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                presentFragmentInternal(fragment);
            }
        });
    }

    private void presentFragmentInternal(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.Modal);
    }

    @Override
    public void dismissFragment(@NonNull final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                dismissFragmentInternal(fragment);
            }
        });
    }

    private void dismissFragmentInternal(AwesomeFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        AwesomeFragment top = (AwesomeFragment)fragmentManager.findFragmentById(android.R.id.content);
        if (top == null) {
            return;
        }
        top.setAnimation(PresentAnimation.Modal);
        AwesomeFragment presented = getPresentedFragment(fragment);
        if (presented != null) {
            fragment.setAnimation(PresentAnimation.Modal);
            top.setUserVisibleHint(false);
            getSupportFragmentManager().popBackStack(fragment.getSceneId(), 0);
            FragmentHelper.executePendingTransactionsSafe(getSupportFragmentManager());
            fragment.onFragmentResult(top.getRequestCode(), top.getResultCode(), top.getResultData());
        } else {
            AwesomeFragment presenting = getPresentingFragment(fragment);
            if (presenting != null) {
                presenting.setAnimation(PresentAnimation.Modal);
            }
            fragment.setUserVisibleHint(false);
            if (presenting == null) {
                ActivityCompat.finishAfterTransition(this);
            } else {
                fragmentManager.popBackStack(fragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);
                presenting.onFragmentResult(fragment.getRequestCode(), fragment.getResultCode(), fragment.getResultData());
            }
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

    public void showDialog(@NonNull final AwesomeFragment dialog, final int requestCode) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                showDialogInternal(dialog, requestCode);
            }
        });
    }

    private void showDialogInternal(AwesomeFragment dialog, int requestCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
           if (fragment != null) {
               dialog.setTargetFragment(fragment, requestCode);
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
        clearFragments();
        FragmentHelper.addFragmentToBackStack(getSupportFragmentManager(), android.R.id.content, fragment, PresentAnimation.None);
    }

    public void clearFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            String tag = fragmentManager.getBackStackEntryAt(0).getName();
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
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
        List<AwesomeFragment> children = getFragmentsAtAddedList();
        for (int i = 0, size = children.size(); i < size; i++) {
            AwesomeFragment child = children.get(i);
            child.onStatusBarTranslucentChanged(translucent);
        }
    }

    public List<AwesomeFragment> getFragmentsAtAddedList() {
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

    @Nullable
    public DialogFragment getDialogFragment() {
        return FragmentHelper.getDialogFragment(getSupportFragmentManager());
    }

    public Window getCurrentWindow() {
        DialogFragment dialogFragment = getDialogFragment();
        if (dialogFragment != null && dialogFragment.isAdded()) {
            return dialogFragment.getDialog().getWindow();
        } else {
            return getWindow();
        }
    }

    protected void scheduleTaskAtStarted(Runnable runnable) {
        lifecycleDelegate.scheduleTaskAtStarted(runnable);
    }

}
