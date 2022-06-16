package com.navigation.toolbar;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.androidx.BarStyle;
import com.navigation.androidx.Style;
import com.navigation.databinding.FragmentViewBindingBinding;
import com.navigation.statusbar.TestStatusBarFragment;

public class ViewBindingFragment extends BaseBindingFragment<FragmentViewBindingBinding> {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.button.setOnClickListener(v -> requireStackFragment().pushFragment(new TestStatusBarFragment()));
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setStatusBarStyle(BarStyle.DarkContent);
    }
}
