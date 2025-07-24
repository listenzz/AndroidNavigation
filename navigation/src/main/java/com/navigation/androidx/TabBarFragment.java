package com.navigation.androidx;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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

public class TabBarFragment extends AwesomeFragment {

    private static final String SAVED_FRAGMENT_TAGS = "nav_fragment_tags";
    private static final String SAVED_SELECTED_INDEX = "nav_selected_index";
    private static final String SAVED_BOTTOM_BAR_HIDDEN = "nav_tab_bar_hidden";
    private static final String SAVED_TAB_BAR_PROVIDER_CLASS_NAME = "nav_tab_bar_provider_class_name";

    private List<AwesomeFragment> mFragments = new ArrayList<>();
    private ArrayList<String> mFragmentTags = new ArrayList<>();

    private int mSelectedIndex;
    private boolean mTabBarHidden;

    private TabBarProvider mTabBarProvider;

    private View mTabBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_fragment_tabbar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedIndex = savedInstanceState.getInt(SAVED_SELECTED_INDEX);
            mTabBarHidden = savedInstanceState.getBoolean(SAVED_BOTTOM_BAR_HIDDEN, false);
        }

        ensureChildFragments(savedInstanceState);
        ensureTabBarProvider(savedInstanceState);

        mTabBar = createTabBar(root, savedInstanceState);

        ViewUtils.applyWindowInsets(getWindow(), root, view -> fitsTabBar());

        if (mTabBarHidden) {
            hideTabBar();
        }
    }

    private void fitsTabBar() {
        ViewUtils.doOnPreDrawOnce(mTabBar, view -> {
            if (shouldFitsNavigationBar()) {
                view.setPadding(0, 0, 0, SystemUI.navigationBarHeight(getWindow()));
            } else {
                view.setPadding(0, 0, 0, 0);
            }
        });
    }

    private void ensureChildFragments(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFragmentTags = savedInstanceState.getStringArrayList(SAVED_FRAGMENT_TAGS);
            FragmentManager fragmentManager = getChildFragmentManager();
            for (int i = 0, size = mFragmentTags.size(); i < size; i++) {
                mFragments.add((AwesomeFragment) fragmentManager.findFragmentByTag(mFragmentTags.get(i)));
            }
            return;
        }

        if (mFragments == null || mFragments.isEmpty()) {
            throw new IllegalArgumentException("必须使用 `setChildFragments` 设置 childFragments");
        }

        inflateChildFragments(mFragments);
    }

    private void ensureTabBarProvider(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String providerClassName = savedInstanceState.getString(SAVED_TAB_BAR_PROVIDER_CLASS_NAME);
            try {
                Class<?> clazz = Class.forName(providerClassName);
                mTabBarProvider = (TabBarProvider) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mTabBarProvider == null) {
            setTabBarProvider(createDefaultTabBarProvider());
        }
    }

    private View createTabBar(@NonNull View root, @Nullable Bundle savedInstanceState) {
        List<TabBarItem> tabBarItems = new ArrayList<>();
        for (int i = 0, size = mFragments.size(); i < size; i++) {
            AwesomeFragment fragment = mFragments.get(i);
            TabBarItem tabBarItem = fragment.getTabBarItem();
            if (tabBarItem == null) {
                tabBarItem = new TabBarItem("TAB" + i);
            }
            tabBarItems.add(tabBarItem);
        }
        View tabBar = mTabBarProvider.onCreateTabBar(tabBarItems, this, savedInstanceState);
        FrameLayout frameLayout = (FrameLayout) root;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        frameLayout.addView(tabBar, layoutParams);
        mTabBarProvider.setSelectedIndex(mSelectedIndex);

        return tabBar;
    }

    private void inflateChildFragments(List<AwesomeFragment> fragments) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0, size = fragments.size(); i < size; i++) {
            AwesomeFragment fragment = fragments.get(i);
            mFragmentTags.add(fragment.getSceneId());
            transaction.add(R.id.tabs_content, fragment, fragment.getSceneId());
            if (i == mSelectedIndex) {
                transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED);
                transaction.setPrimaryNavigationFragment(fragment);
            } else {
                transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED);
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVED_FRAGMENT_TAGS, mFragmentTags);
        outState.putInt(SAVED_SELECTED_INDEX, mSelectedIndex);
        outState.putBoolean(SAVED_BOTTOM_BAR_HIDDEN, mTabBarHidden);
        if (mTabBarProvider != null) {
            outState.putString(SAVED_TAB_BAR_PROVIDER_CLASS_NAME, mTabBarProvider.getClass().getName());
            mTabBarProvider.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroyView() {
        if (mTabBarProvider != null) {
            mTabBarProvider.onDestroyTabBar();
            mTabBar = null;
        }
        super.onDestroyView();
    }

    @Override
    public boolean isLeafAwesomeFragment() {
        return false;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getSelectedFragment();
    }

    @Override
    protected int preferredNavigationBarColor() {
        if (mTabBarHidden) {
            return super.preferredNavigationBarColor();
        }

        if (SystemUI.isGestureNavigationEnabled(getContentResolver())) {
            return Color.TRANSPARENT;
        }

        if (mStyle.getNavigationBarColor() != Style.INVALID_COLOR) {
            return mStyle.getNavigationBarColor();
        }

        return Color.parseColor(mStyle.getTabBarBackgroundColor());
    }

    @Override
    protected boolean shouldFitsNavigationBar() {
        if (preferredNavigationBarHidden()) {
            return false;
        }

        return SystemUI.isGestureNavigationEnabled(getContentResolver()) || AppUtils.isOpaque(preferredNavigationBarColor());
    }

    public void setChildFragments(AwesomeFragment... fragments) {
        setChildFragments(Arrays.asList(fragments));
    }

    public void setChildFragments(final List<AwesomeFragment> fragments) {
        if (isAdded()) {
            throw new IllegalStateException("TabBarFragment 已经处于 added 状态，不能再设置 childFragments");
        }
        mFragments = fragments;
    }

    @NonNull
    @Override
    public List<AwesomeFragment> getChildAwesomeFragments() {
        return mFragments;
    }

    public void setSelectedFragment(AwesomeFragment fragment) {
        setSelectedFragment(fragment, () -> {
        });
    }

    public void setSelectedFragment(AwesomeFragment fragment, @NonNull Runnable completion) {
        int index = mFragments.indexOf(fragment);
        setSelectedIndex(index, completion);
    }

    @Nullable
    public AwesomeFragment getSelectedFragment() {
        if (mFragments == null) {
            return null;
        }

        AwesomeFragment fragment = mFragments.get(getSelectedIndex());
        if (fragment.isAdded()) {
            return fragment;
        }

        return null;
    }

    @NonNull
    public AwesomeFragment requireSelectedFragment() {
        AwesomeFragment fragment = getSelectedFragment();
        if (fragment == null) {
            throw new NullPointerException("No selected fragment.");
        }
        return fragment;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, () -> {
        });
    }

    public void setSelectedIndex(int index, @NonNull Runnable completion) {
        if (isAdded()) {
            scheduleTaskAtStarted(() -> setSelectedIndexSync(index, completion));
        } else {
            mSelectedIndex = index;
            completion.run();
        }
    }

    public void setTabBarSelectedIndex(int index) {
        if (mTabBarProvider != null) {
            mTabBarProvider.setSelectedIndex(index);
        }
    }

    private void setSelectedIndexSync(int index, @NonNull Runnable completion) {
        setTabBarSelectedIndex(index);

        if (mSelectedIndex == index) {
            completion.run();
        } else {
            mSelectedIndex = index;
            AwesomeFragment current = switchChildFragment(index, completion);
            setTabBarVisibility(current);
        }
    }

    private void setTabBarVisibility(AwesomeFragment current) {
        StackFragment stackFragment = current.getStackFragment();
        if (stackFragment == null || stackFragment.getTabBarFragment() != this) {
            return;
        }

        if (stackFragment.shouldShowTabBarWhenPushed()) {
            showTabBar();
            return;
        }

        if (stackFragment.getTopFragment() == stackFragment.getRootFragment()) {
            showTabBar();
            return;
        }

        hideTabBar();
    }

    @NonNull
    private AwesomeFragment switchChildFragment(int index, @Nullable Runnable completion) {
        FragmentManager fragmentManager = getChildFragmentManager();
        AwesomeFragment precursor = (AwesomeFragment) fragmentManager.getPrimaryNavigationFragment();
        AwesomeFragment current = mFragments.get(index);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setPrimaryNavigationFragment(current);
        if (precursor != null && precursor.isAdded()) {
            setPresentAnimation(current, precursor);
            transaction.setMaxLifecycle(precursor, Lifecycle.State.STARTED);
            transaction.hide(precursor);
        }
        transaction.setMaxLifecycle(current, Lifecycle.State.RESUMED);
        transaction.show(current);
        transaction.commit();

        if (completion != null) {
            completion.run();
        }
        return current;
    }

    protected void setPresentAnimation(AwesomeFragment current, AwesomeFragment previous) {
        current.setAnimation(TransitionAnimation.None);
        previous.setAnimation(TransitionAnimation.None);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getTabBar() {
        return (T) mTabBar;
    }

    public void setTabBarProvider(TabBarProvider tabBarProvider) {
        mTabBarProvider = tabBarProvider;
    }

    protected TabBarProvider createDefaultTabBarProvider() {
        return new DefaultTabBarProvider();
    }

    public void updateTabBar(Bundle options) {
        if (mTabBarProvider != null && options != null) {
            mTabBarProvider.updateTabBar(options);
        }
    }

    void showTabBarAnimated(Animation anim) {
        if (anim == null) {
            showTabBar();
            return;
        }

        mTabBarHidden = false;
        mTabBar.setVisibility(View.GONE);
        handleTabBarVisibilityAnimated(anim);
    }

    void hideTabBarAnimated(Animation anim) {
        if (anim == null) {
            hideTabBar();
            return;
        }

        mTabBarHidden = true;
        mTabBar.setVisibility(View.GONE);
        handleTabBarVisibilityAnimated(anim);
    }

    private void handleTabBarVisibilityAnimated(@NonNull Animation animation) {
        setNeedsNavigationBarAppearanceUpdate();
        mTabBar.postDelayed(() -> {
            if (isAdded()) {
                mTabBar.setVisibility(mTabBarHidden ? View.GONE : View.VISIBLE);
            }
        }, animation.getDuration());
    }

    private void showTabBar() {
        mTabBarHidden = false;
        mTabBar.setVisibility(View.VISIBLE);
        setNeedsNavigationBarAppearanceUpdate();
    }

    private void hideTabBar() {
        mTabBarHidden = true;
        mTabBar.setVisibility(View.GONE);
        setNeedsNavigationBarAppearanceUpdate();
    }
}
