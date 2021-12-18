package com.navigation.androidx;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarFragment extends AwesomeFragment {

    private static final String SAVED_FRAGMENT_TAGS = "nav_fragment_tags";
    private static final String SAVED_SELECTED_INDEX = "nav_selected_index";
    private static final String SAVED_BOTTOM_BAR_HIDDEN = "nav_tab_bar_hidden";
    private static final String SAVED_TAB_BAR_PROVIDER_CLASS_NAME = "nav_tab_bar_provider_class_name";

    private List<AwesomeFragment> fragments = new ArrayList<>();
    private ArrayList<String> fragmentTags = new ArrayList<>();

    private int selectedIndex;
    private boolean tabBarHidden;

    private TabBarProvider tabBarProvider = new DefaultTabBarProvider();

    private View tabBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_fragment_tabbar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        // fragments
        if (savedInstanceState != null) {
            fragmentTags = savedInstanceState.getStringArrayList(SAVED_FRAGMENT_TAGS);
            FragmentManager fragmentManager = getChildFragmentManager();
            for (int i = 0, size = fragmentTags.size(); i < size; i++) {
                fragments.add((AwesomeFragment) fragmentManager.findFragmentByTag(fragmentTags.get(i)));
            }
            selectedIndex = savedInstanceState.getInt(SAVED_SELECTED_INDEX);
            tabBarHidden = savedInstanceState.getBoolean(SAVED_BOTTOM_BAR_HIDDEN, false);
            String providerClassName = savedInstanceState.getString(SAVED_TAB_BAR_PROVIDER_CLASS_NAME);
            if (providerClassName != null) {
                try {
                    Class<?> clazz = Class.forName(providerClassName);
                    tabBarProvider = (TabBarProvider) clazz.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (fragments == null || fragments.size() == 0) {
                throw new IllegalArgumentException("必须使用 `setChildFragments` 设置 childFragments ");
            }
            setChildFragmentsSync(fragments);
        }

        // create TabBar if needed
        if (tabBarProvider != null) {
            List<TabBarItem> tabBarItems = new ArrayList<>();
            for (int i = 0, size = fragments.size(); i < size; i++) {
                AwesomeFragment fragment = fragments.get(i);
                TabBarItem tabBarItem = fragment.getTabBarItem();
                if (tabBarItem == null) {
                    tabBarItem = new TabBarItem("TAB" + i);
                }
                tabBarItems.add(tabBarItem);
            }
            View tabBar = tabBarProvider.onCreateTabBar(tabBarItems, this, savedInstanceState);
            FrameLayout frameLayout = (FrameLayout) root;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            frameLayout.addView(tabBar, layoutParams);
            tabBarProvider.setSelectedIndex(selectedIndex);
            this.tabBar = tabBar;
        }

        if (savedInstanceState != null) {
            setSelectedIndexSync(selectedIndex, null);
            if (tabBarHidden && getTabBar() != null) {
                hideTabBar();
            }
        }
    }

    private void setChildFragmentsSync(List<AwesomeFragment> fragments) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            AwesomeFragment fragment = fragments.get(i);
            fragmentTags.add(fragment.getSceneId());
            transaction.add(R.id.tabs_content, fragment, fragment.getSceneId());
            if (i == selectedIndex) {
                transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED);
                transaction.setPrimaryNavigationFragment(fragment);
            } else {
                transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED);
                transaction.hide(fragment);
            }
        }
        transaction.commit();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVED_FRAGMENT_TAGS, fragmentTags);
        outState.putInt(SAVED_SELECTED_INDEX, selectedIndex);
        outState.putBoolean(SAVED_BOTTOM_BAR_HIDDEN, tabBarHidden);
        if (tabBarProvider != null) {
            outState.putString(SAVED_TAB_BAR_PROVIDER_CLASS_NAME, tabBarProvider.getClass().getName());
            tabBarProvider.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        if (tabBarProvider != null) {
            tabBarProvider.onDestroyTabBar();
            tabBar = null;
        }
        super.onDestroyView();
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getSelectedFragment();
    }

    @Override
    protected AwesomeFragment childFragmentForNavigationBarAppearance() {
        if (tabBarHidden) {
            return super.childFragmentForNavigationBarAppearance();
        }
        return null;
    }

    @Override
    protected int preferredNavigationBarColor() {
        if (mStyle.getNavigationBarColor() != Style.INVALID_COLOR) {
            return mStyle.getNavigationBarColor();
        } else {
            return Color.parseColor(mStyle.getTabBarBackgroundColor());
        }
    }

    public void setChildFragments(AwesomeFragment... fragments) {
        setChildFragments(Arrays.asList(fragments));
    }

    public void setChildFragments(final List<AwesomeFragment> fragments) {
        if (isAdded()) {
            throw new IllegalStateException("TabBarFragment 已经处于 added 状态，不能再设置 childFragments");
        }
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public List<AwesomeFragment> getChildFragments() {
        return fragments;
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        setSelectedFragment(fragment, null);
    }

    public void setSelectedFragment(AwesomeFragment fragment, @Nullable Runnable completion) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index, completion);
    }

    @Nullable
    public AwesomeFragment getSelectedFragment() {
        if (fragments == null) {
            return null;
        }
        AwesomeFragment selectedFragment = fragments.get(getSelectedIndex());
        if (selectedFragment.isAdded()) {
            return selectedFragment;
        }
        return null;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, null);
    }

    public void setSelectedIndex(int index, @Nullable Runnable completion) {
        if (isAdded()) {
            scheduleTaskAtStarted(() -> setSelectedIndexSync(index, completion));
        } else {
            selectedIndex = index;
            if (completion != null) {
                throw new IllegalStateException("Can't run completion callback when the fragment is not added.");
            }
        }
    }

    private void setSelectedIndexSync(int index, @Nullable Runnable completion) {
        if (tabBarProvider != null) {
            tabBarProvider.setSelectedIndex(index);
        }

        if (selectedIndex == index) {
            if (completion != null) {
                completion.run();
            }
            return;
        }

        selectedIndex = index;
        FragmentManager fragmentManager = getChildFragmentManager();
        AwesomeFragment previous = (AwesomeFragment) fragmentManager.getPrimaryNavigationFragment();
        AwesomeFragment current = fragments.get(index);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setPrimaryNavigationFragment(current);
        if (previous != null && previous.isAdded()) {
            setPresentAnimation(current, previous);
            transaction.setMaxLifecycle(previous, Lifecycle.State.STARTED);
            transaction.hide(previous);
        }
        transaction.setMaxLifecycle(current, Lifecycle.State.RESUMED);
        transaction.show(current);
        transaction.commit();
        FragmentHelper.executePendingTransactionsSafe(fragmentManager);

        if (completion != null) {
            completion.run();
        }

        if (tabBar != null) {
            StackFragment nav = current.getStackFragment();
            if (nav != null && nav.getTabBarFragment() == this && nav.shouldHideTabBarWhenPushed()) {
                if (nav.getTopFragment() == nav.getRootFragment()) {
                    showTabBar();
                } else {
                    hideTabBar();
                }
            } else {
                showTabBar();
            }
        }
    }

    protected void setPresentAnimation(AwesomeFragment current, AwesomeFragment previous) {
        current.setAnimation(TransitionAnimation.None);
        previous.setAnimation(TransitionAnimation.None);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends View> T getTabBar() {
        return (T) tabBar;
    }

    public void setTabBarProvider(TabBarProvider tabBarProvider) {
        this.tabBarProvider = tabBarProvider;
    }

    public TabBarProvider getTabBarProvider() {
        return tabBarProvider;
    }

    public void updateTabBar(Bundle options) {
        if (this.tabBarProvider != null && options != null) {
            this.tabBarProvider.updateTabBar(options);
        }
    }

    void showTabBarAnimated(Animation anim) {
        if (anim == null) {
            showTabBar();
            return;
        }

        if (tabBar != null) {
            tabBarHidden = false;
            tabBar.setVisibility(View.GONE);
            handleTabBarVisibilityAnimated(anim);
        }
    }

    void hideTabBarAnimated(Animation anim) {
        if (anim == null) {
            hideTabBar();
            return;
        }

        if (tabBar != null) {
            tabBarHidden = true;
            tabBar.setVisibility(View.GONE);
            handleTabBarVisibilityAnimated(anim);
        }
    }

    private void handleTabBarVisibilityAnimated(@NonNull Animation animation) {
        setNeedsNavigationBarAppearanceUpdate();
        tabBar.postDelayed(() -> {
            if (isAdded()) {
                tabBar.setVisibility(tabBarHidden ? View.GONE : View.VISIBLE);
            }
        } , animation.getDuration());
    }

    private void showTabBar() {
        if (tabBar != null) {
            tabBarHidden = false;
            tabBar.setVisibility(View.VISIBLE);
            setNeedsNavigationBarAppearanceUpdate();
        }
    }

    private void hideTabBar() {
        if (tabBar != null) {
            tabBarHidden = true;
            tabBar.setVisibility(View.GONE);
            setNeedsNavigationBarAppearanceUpdate();
        }
    }
}
