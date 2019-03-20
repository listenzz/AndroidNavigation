package me.listenzz.navigation;

import android.support.annotation.NonNull;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(@NonNull AwesomeFragment fragment);

    void dismissFragment(@NonNull AwesomeFragment fragment);

    AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment);

    void setActivityRootFragment(AwesomeFragment root);

    @NonNull
    Style getStyle();

    void setStatusBarTranslucent(boolean under);

    boolean isStatusBarTranslucent();

    boolean activityHasFormerRoot();

}
