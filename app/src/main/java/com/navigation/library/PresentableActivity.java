package com.navigation.library;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(AwesomeFragment fragment);

    void dismissFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentedFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(AwesomeFragment fragment);

    Style getStyle();

    void setContentUnderStatusBar(boolean under);

    boolean isContentUnderStatusBar();

}
