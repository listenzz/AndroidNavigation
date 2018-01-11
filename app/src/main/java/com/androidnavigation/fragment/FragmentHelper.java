package com.androidnavigation.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Listen on 2018/1/11.
 */

public class FragmentHelper {

    public static Bundle getArguments(Fragment fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
            fragment.setArguments(args);
        }
        return args;
    }


}
