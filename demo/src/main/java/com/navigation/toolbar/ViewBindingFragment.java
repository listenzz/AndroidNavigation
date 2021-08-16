package com.navigation.toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.databinding.FragmentViewBindingBinding;


public class ViewBindingFragment extends BaseFragment {
    FragmentViewBindingBinding bindingBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_view_binding, container, false);
        bindingBinding = FragmentViewBindingBinding.bind(root.getChildAt(0));
        return root;
    }
}
