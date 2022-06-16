package com.navigation.toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.navigation.BaseFragment;

public class BaseBindingFragment<T extends ViewBinding> extends BaseFragment {
    protected T mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        // see: https://github.com/listenzz/AndroidNavigation/issues/52
        mBinding = ViewBindingUtil.create(getClass(), LayoutInflater.from(getActivity()), container, false);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.addView(mBinding.getRoot());
        return frameLayout;
    }

}
