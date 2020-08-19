package com.navigation.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.navigation.R;
import com.navigation.androidx.AwesomeFragment;
import com.navigation.databinding.FragmentDataBindingBinding;

public class DataBindingDialogFragment extends AwesomeFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_data_binding, container, false);

        FragmentDataBindingBinding bindingBinding = DataBindingUtil.bind(root.getChildAt(0));

        User user = new User();
        user.setFirstName("Jack");
        user.setLastName("Ma");
        bindingBinding.setUser(user);

        return root;
    }
}
