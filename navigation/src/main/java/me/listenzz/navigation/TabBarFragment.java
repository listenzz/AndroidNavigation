package me.listenzz.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarFragment extends AwesomeFragment {

    private static final String SAVED_FRAGMENT_TAGS = "nav_fragment_tags";
    private static final String SAVED_SELECTED_INDEX = "nav_selected_index";
    private static final String SAVED_BOTTOM_BAR_HIDDEN = "nav_tab_bar_hidden";
    private static final String SAVED_TAB_BAR_PROVIDER_CLASS_NAME = "nav_tab_bar_provider_class_name";

    private List<AwesomeFragment> fragments;
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
            fragments = new ArrayList<>();
            FragmentManager fragmentManager = getChildFragmentManager();
            for (int i = 0, size = fragmentTags.size(); i < size; i++) {
                fragments.add((AwesomeFragment) fragmentManager.findFragmentByTag(fragmentTags.get(i)));
            }
            selectedIndex = savedInstanceState.getInt(SAVED_SELECTED_INDEX);
            tabBarHidden = savedInstanceState.getBoolean(SAVED_BOTTOM_BAR_HIDDEN, false);
            String providerClassName = savedInstanceState.getString(SAVED_TAB_BAR_PROVIDER_CLASS_NAME);
            tabBarProvider = null;
            if (providerClassName != null) {
                try {
                    Class clazz = Class.forName(providerClassName);
                    tabBarProvider = (TabBarProvider) clazz.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (fragments == null || fragments.size() == 0) {
                throw new IllegalArgumentException("必须使用 `setChildFragments` 设置 childFragments ");
            }
            setChildFragmentsInternal(fragments);
        }

        // create TabBar if needed
        if (tabBarProvider != null) {
            List<TabBarItem> tabBarItems = new ArrayList<>();
            for (int i = 0, size = fragments.size(); i < size; i++) {
                AwesomeFragment fragment = fragments.get(i);
                TabBarItem tabBarItem = fragment.getTabBarItem();
                tabBarItems.add(tabBarItem);
            }
            View tabBar = tabBarProvider.onCreateTabBar(tabBarItems, this, savedInstanceState);
            FrameLayout frameLayout = (FrameLayout) root;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            frameLayout.addView(tabBar, layoutParams);
            this.tabBar = tabBar;
        }

        if (savedInstanceState != null) {
            setSelectedIndex(selectedIndex);
            if (tabBarHidden && getTabBar() != null) {
                getTabBar().setVisibility(View.GONE);
            }
        }
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
    protected Integer preferredNavigationBarColor() {
        Integer color = super.preferredNavigationBarColor();
        if (color != null) {
            return color;
        }
        return tabBarHidden ? null : Color.parseColor(style.getTabBarBackgroundColor());
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

    public List<AwesomeFragment> getChildFragments() {
        return fragments;
    }

    private void setChildFragmentsInternal(List<AwesomeFragment> fragments) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            AwesomeFragment fragment = fragments.get(i);
            fragmentTags.add(fragment.getSceneId());
            transaction.add(R.id.tabs_content, fragment, fragment.getSceneId());
            if (i == 0) {
                transaction.setPrimaryNavigationFragment(fragment);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        int index = fragments.indexOf(fragment);
        setSelectedIndex(index);
    }

    public AwesomeFragment getSelectedFragment() {
        AwesomeFragment selectedFragment = fragments.get(getSelectedIndex());
        if (selectedFragment.isAdded()) {
            return selectedFragment;
        }
        return null;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(final int index) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                if (tabBarProvider != null) {
                    tabBarProvider.setSelectedIndex(index);
                }

                if (selectedIndex == index) {
                    return;
                }

                selectedIndex = index;
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentHelper.executePendingTransactionsSafe(fragmentManager);
                Fragment previous = fragmentManager.getPrimaryNavigationFragment();
                AwesomeFragment current = fragments.get(index);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setPrimaryNavigationFragment(current);
                transaction.hide(previous);
                transaction.show(current);
                transaction.commit();

                if (tabBar != null) {
                    NavigationFragment navigationFragment = current.getNavigationFragment();
                    if (navigationFragment != null && navigationFragment.shouldHideTabBarWhenPushed()) {
                        if (navigationFragment.getChildFragmentCountAtBackStack() <= 1) {
                            tabBarHidden = false;
                            tabBar.setVisibility(View.VISIBLE);
                        } else {
                            tabBarHidden = true;
                            tabBar.setVisibility(View.GONE);
                        }
                    } else {
                        tabBarHidden = false;
                        tabBar.setVisibility(View.VISIBLE);
                    }
                    setNeedsNavigationBarAppearanceUpdate();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getTabBar() {
        return (T) tabBar;
    }

    public void setTabBarProvider(TabBarProvider tabBarProvider) {
        this.tabBarProvider = tabBarProvider;
    }

    void showTabBarWhenPop(@AnimRes int anim) {
        if (tabBar != null) {
            tabBarHidden = false;
            setNeedsNavigationBarAppearanceUpdate();
            if (anim != R.anim.nav_none) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
                animation.setAnimationListener(new TabBarAnimationListener(false));
                tabBar.startAnimation(animation);
            } else {
                tabBar.setVisibility(View.VISIBLE);
            }
        }

    }

    void hideTabBarWhenPush(@AnimRes int anim) {
        if (tabBar != null) {
            tabBarHidden = true;
            setNeedsNavigationBarAppearanceUpdate();
            if (anim != R.anim.nav_none) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
                animation.setAnimationListener(new TabBarAnimationListener(true));
                tabBar.startAnimation(animation);
            } else {
                tabBar.setVisibility(View.GONE);
            }
        }
    }

    class TabBarAnimationListener implements Animation.AnimationListener {

        boolean hidden;

        TabBarAnimationListener(boolean hidden) {
            this.hidden = hidden;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (hidden && tabBar != null) {
                tabBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!hidden && tabBar != null) {
                tabBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
