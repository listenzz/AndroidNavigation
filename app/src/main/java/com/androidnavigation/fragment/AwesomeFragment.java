package com.androidnavigation.fragment;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Listen on 2018/1/11.
 */

public class AwesomeFragment extends Fragment implements LifecycleObserver, FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "AndroidNavigation";

    public static final String ARGS_SCENE_ID = "scene_id";
    public static final String ARGS_REQUEST_CODE = "request_code";

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(View view) {
        if (view == null || view.getContext() == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // ------- lifecycle methods -------

    private PresentableActivity presentableActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof PresentableActivity)) {
            throw new IllegalArgumentException("Activity must implements PresentableActivity!");
        }
        presentableActivity = (PresentableActivity) activity;
    }

    @Override
    public void onDetach() {
        presentableActivity = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(this);
        getChildFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onDestroy() {
        getChildFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        PresentAnimation animation = getAnimation();
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                return AnimationUtils.loadAnimation(getContext(), animation.enter);
            } else {
                return AnimationUtils.loadAnimation(getContext(), animation.exit);
            }
        } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
            if (enter) {
                return AnimationUtils.loadAnimation(getContext(), animation.popEnter);
            } else {
                return AnimationUtils.loadAnimation(getContext(), animation.popExit);
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, getClass().getSimpleName() + " Entry index:" + entry.getId() + " tag:" + entry.getName());
        }
    }

    // ------ lifecycle arch -------

    private boolean active;
    private LinkedList<Runnable> tasks = new LinkedList<>();

    protected void scheduleTask(Runnable runnable) {
        if (getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
            tasks.add(runnable);
            considerExecute();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onStateChange() {
        if (getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            // 清空队列
            tasks.clear();
            getLifecycle().removeObserver(this);
        } else {
            activeStateChanged(isActiveState(getLifecycle().getCurrentState()));
        }
    }

    protected void activeStateChanged(boolean newActive) {
        if (newActive != this.active) {
            this.active = newActive;
            considerExecute();
        }
    }

    private void considerExecute() {
        if (active) {
            if (isActiveState(getLifecycle().getCurrentState())) {
                if (tasks.size() > 0) {
                    for (Runnable task : tasks) {
                        task.run();
                    }
                    tasks.clear();
                }
            }
        }
    }

    protected boolean isActiveState(Lifecycle.State state) {
        return state.isAtLeast(Lifecycle.State.STARTED);
    }

    protected boolean isAtLeastStarted() {
        return isActiveState(getLifecycle().getCurrentState());
    }

    // ------- navigation ------

    private String sceneId;

    public String getSceneId() {
        if (this.sceneId == null) {
            Bundle args = FragmentHelper.getArguments(this);
            String sceneId = args.getString(ARGS_SCENE_ID);
            if (sceneId == null) {
                sceneId = UUID.randomUUID().toString();
                args.putString(ARGS_SCENE_ID, sceneId);
            }

            this.sceneId = sceneId;
        }
        return this.sceneId;
    }

    public void presentFragment(AwesomeFragment fragment, int requestCode, PresentAnimation animation) {
        if (presentableActivity != null) {
            Bundle args = FragmentHelper.getArguments(fragment);
            args.putInt(ARGS_REQUEST_CODE, requestCode);
            presentableActivity.presentFragment(fragment, animation);
        }
    }

    public void dismissFragment(AwesomeFragment fragment, PresentAnimation animation) {
        if (presentableActivity != null) {
            presentableActivity.dismissFragment(fragment, animation);
        }
    }

    private int requestCode;
    private int resultCode;
    private Bundle result;

    public void setResult(int resultCode, Bundle data) {
        this.result = data;
        this.resultCode = resultCode;
    }

    public int getRequestCode() {
        if (requestCode == 0) {
            Bundle args = FragmentHelper.getArguments(this);
            requestCode = args.getInt(ARGS_REQUEST_CODE);
        }
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Bundle getResultData() {
        return result;
    }

    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        Log.i(TAG, toString() + "#onFragmentResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        List<AwesomeFragment> fragments = getFragments();
        for (AwesomeFragment child : fragments) {
            child.onFragmentResult(requestCode, resultCode, data);
        }
    }

    public boolean dispatchBackPressed() {
        FragmentManager fragmentManager =  getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
        if (fragment != null) {
            AwesomeFragment child = (AwesomeFragment) fragment;
            return child.dispatchBackPressed() || onBackPressed();
        } else if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(count -1);
            AwesomeFragment child = (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
            return child.dispatchBackPressed() || onBackPressed();
        } else {
            return onBackPressed();
        }
    }

    protected boolean onBackPressed() {
        return false;
    }

    public AwesomeFragment getPresentedFragment() {
        return presentableActivity.getPresentedFragment(this);
    }

    public AwesomeFragment getPresentingFragment() {
        return presentableActivity.getPresentingFragment(this);
    }

    public String getDebugTag() {
        if (getActivity() == null) {
            return null;
        }
        AwesomeFragment parent =  getParent();
        if (parent == null) {
            return getActivity().getClass().getSimpleName() + "#" + findIndexAtBackStack();
        } else {
            return parent.getDebugTag() + "-" + getClass().getSimpleName() + "#" + findIndexAtBackStack();
        }
    }

    protected int findIndexAtBackStack() {
        FragmentManager fragmentManager = getFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        int index = -1;
        for (int i = 0; i < count; i++) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
            if (getTag().equals(backStackEntry.getName())) {
                index = i;
            }
        }
        return index;
    }

    public AwesomeFragment getParent() {
        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof AwesomeFragment) {
            return (AwesomeFragment) fragment;
        }
        return null;
    }

    public List<AwesomeFragment> getFragments() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        List<AwesomeFragment> children = new ArrayList<>();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            children.add((AwesomeFragment) fragments.get(i));
        }
        return children;
    }

    private PresentAnimation animation = PresentAnimation.Fade;

    public void setAnimation(PresentAnimation animation) {
        this.animation = animation;
    }

    public PresentAnimation getAnimation() {
        return animation;
    }

    // ------- statusBar --------

    public StatusBarStyle statusBarStyle() {
        return StatusBarStyle.LightContent;
    }

    public boolean isStatusBarHidden() {
        return false;
    }

    public Animation statusBarUpdateAnimation() {
        return null;
    }

    public void updateStatusBarAppearance() {

    }

    public AwesomeFragment childFragmentForStatusBarStyle() {
        return this;
    }

    public AwesomeFragment childFragmentForStatusBarHidden() {
        return this;
    }

    // ------ NavigationFragment -----

    public boolean hidesBottomBarWhenPushed() {
        return false;
    }

    public NavigationFragment getNavigatoinFragment() {
        if (this instanceof NavigationFragment) {
            return (NavigationFragment) this;
        }
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getNavigatoinFragment();
        }
        return null;
    }

    public NavigationItem getNavigationItem() {
        return null;
    }

    // ------ TabBarFragment -------

    public TabBarFragment getTabBarFragment() {
        if (this instanceof TabBarFragment) {
            return (TabBarFragment) this;
        }
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getTabBarFragment();
        }
        return null;
    }

    public TabBarItem getTabBarItem() {
        return null;
    }

    // ------ DrawerFragment -------

    public DrawerFragment getDrawerFragment() {
        if (this instanceof DrawerFragment) {
            return (DrawerFragment) this;
        }
        AwesomeFragment parent = getParent();
        if (parent != null) {
            return parent.getDrawerFragment();
        }
        return null;
    }

}
