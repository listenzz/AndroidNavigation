package com.navigation.fragment;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(AwesomeFragment fragment);

    void dismissFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentedFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(AwesomeFragment fragment);

}
