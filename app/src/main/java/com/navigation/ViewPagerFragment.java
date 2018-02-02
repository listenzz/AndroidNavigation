package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.fragment.AwesomeFragment;

/**
 * Created by Listen on 2018/2/1.
 */

public class ViewPagerFragment extends AwesomeFragment {

    Toolbar toolbar;

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected Toolbar onCreateToolbar(View parent) {
        return toolbar;
    }

    @Override
    protected int preferredStatusBarColor() {
        return Color.parseColor("#3F51B5");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_pager, container, false);
        initView(root);
        return root;
    }

    private void initView(View view) {

        toolbar = view.findViewById(R.id.toolbar);
        AppBarLayout appBarLayout = view.findViewById(R.id.appbar_layout);

        // important
        appendStatusBarPaddingAndHeight(appBarLayout, -2);

        TabLayout tabLayout =  view.findViewById(R.id.tab_layout);
        ViewPager viewPager =  view.findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText("One"));
        tabLayout.addTab(tabLayout.newTab().setText("Tow"));
        tabLayout.addTab(tabLayout.newTab().setText("Three"));

        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), "One", "Tow", "Three"));
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
