package com.navigation.androidx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface PresentableActivity {

    void presentFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation);

    void dismissFragment(@NonNull AwesomeFragment fragment, @NonNull Runnable completion, @NonNull TransitionAnimation animation);

    AwesomeFragment getPresentedFragment(@NonNull AwesomeFragment fragment);

    AwesomeFragment getPresentingFragment(@NonNull AwesomeFragment fragment);

    void setActivityRootFragment(AwesomeFragment root);

    @Nullable
    Style getStyle();

     String ARG_PRESENTING_SCENE_ID = "ARG_PRESENTING_SCENE_ID";

}
