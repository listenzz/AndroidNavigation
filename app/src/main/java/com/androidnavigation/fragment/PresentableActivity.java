package com.androidnavigation.fragment;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(AwesomeFragment fragment, PresentAnimation animation);

    void dismissFragment(AwesomeFragment fragment, PresentAnimation animation);

    AwesomeFragment getPresentedFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(AwesomeFragment fragment);

}
