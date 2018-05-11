package me.listenzz.navigation;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(AwesomeFragment fragment);

    void dismissFragment(AwesomeFragment fragment);

    void presentFragment(AwesomeFragment fragment, Anim anim);

    void dismissFragment(AwesomeFragment fragment, Anim anim);

    AwesomeFragment getPresentedFragment(AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(AwesomeFragment fragment);

    void setActivityRootFragment(AwesomeFragment root);

    Style getStyle();

    void setStatusBarTranslucent(boolean under);

    boolean isStatusBarTranslucent();

}
