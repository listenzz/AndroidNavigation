package com.navigation.toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.AwesomeFragment;
import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.Style;
import com.navigation.androidx.SystemUI;

public class ViewPagerFragment extends BaseFragment {

    AwesomeToolbar toolbar;

    AppBarLayout appBarLayout;

    @Override
    public boolean isLeafAwesomeFragment() {
        return false;
    }

    @Nullable
    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        // 可以重写这个方法来指定由那个子 Fragment 来决定系统 UI（状态栏，导航栏）的样式，默认由容器本身决定
        return super.childFragmentForAppearance();
    }

    @Override
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return toolbar;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
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

        appBarLayout = view.findViewById(R.id.appbar_layout);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText("Android"));
        tabLayout.addTab(tabLayout.newTab().setText("Awesome"));
        tabLayout.addTab(tabLayout.newTab().setText("Navigation"));

        viewPager.setAdapter(new ViewPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String[] titles = new String[]{"Android", "Awesome", "Navigation"};
            tab.setText(titles[position]);
            viewPager.setCurrentItem(position);
        }).attach();

        viewPager.setCurrentItem(0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Toolbar In AppBar");
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String[] titles = new String[]{"Android", "Awesome", "Navigation"};
            return PageFragment.newInstance(titles[position]);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

}
