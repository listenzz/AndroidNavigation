package com.navigation.dialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;
import com.navigation.databinding.FragmentDataBindingBinding;

import me.listenzz.navigation.AwesomeFragment;

public class DataBindingDialogFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_data_binding, container, false);

        FragmentDataBindingBinding bindingBinding = DataBindingUtil.bind(root.getChildAt(0));

        User user = new User();
        user.setFirstName("LI");
        user.setLastName("sheng");
        bindingBinding.setUser(user);

        return root;
    }
}
