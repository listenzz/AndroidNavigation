package com.navigation.androidx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Listen on 2018/1/11.
 */

public interface PresentableActivity {

    void presentFragment(@NonNull AwesomeFragment fragment);

    void dismissFragment(@NonNull AwesomeFragment fragment);

    AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment);

    void setActivityRootFragment(AwesomeFragment root);

    @Nullable
    Style getStyle();

    void setStatusBarTranslucent(boolean under);

    boolean isStatusBarTranslucent();
}
