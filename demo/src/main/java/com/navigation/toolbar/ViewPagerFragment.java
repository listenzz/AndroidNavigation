package com.navigation.toolbar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.BaseFragment;
import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;

import me.listenzz.navigation.AwesomeToolbar;
import me.listenzz.navigation.Style;


/**
 * Created by Listen on 2018/2/1.
 */

public class ViewPagerFragment extends BaseFragment {

    AwesomeToolbar toolbar;

    int location;

    @Override
    public boolean isParentFragment() {
        return true;
    }


    @Nullable
    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        // 可以重写这个方法来指定由那个子 Fragment 来决定系统 UI（状态栏）的样式，否则由容器本身决定
        return super.childFragmentForAppearance();
    }

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return toolbar;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setShadow(null);
        style.setElevation(0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_pager, container, false);
        initView(root);
        return root;
    }

    private void initView(View view) {

        toolbar = view.findViewById(R.id.toolbar);

        AppBarLayout appBarLayout = view.findViewById(R.id.appbar_layout);

        // important
        if(isStatusBarTranslucent()) {
            appendStatusBarPadding(appBarLayout, -2);
        }

        TabLayout tabLayout =  view.findViewById(R.id.tab_layout);
        ViewPager viewPager =  view.findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText("Android"));
        tabLayout.addTab(tabLayout.newTab().setText("Awesome"));
        tabLayout.addTab(tabLayout.newTab().setText("Navigation"));

        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), "Android", "Awesome", "Navigation"));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                location = position;
                setNeedsStatusBarAppearanceUpdate();
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle("Toolbar In AppBar");
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        String[] titles;

        public ViewPagerAdapter(FragmentManager fm, String... titles) {
            super(fm);
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            String[] titles = new String[] {"Android", "Awesome", "Navigation"};
            return PageFragment.newInstance(titles[position]);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

}
