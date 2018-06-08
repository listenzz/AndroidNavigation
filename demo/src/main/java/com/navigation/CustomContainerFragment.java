package com.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.listenzz.navigation.AwesomeFragment;
import me.listenzz.navigation.FragmentHelper;
import me.listenzz.navigation.PresentAnimation;

public class CustomContainerFragment extends AwesomeFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.tower, firsFloorFragment, PresentAnimation.None);
        }
    }

    AwesomeFragment firsFloorFragment;

    public void setFirsFloorFragment(final AwesomeFragment firsFloorFragment) {
        this.firsFloorFragment = firsFloorFragment;
    }

    @Override
    public int getPresentContainerId() {
        return R.id.tower;
    }
}
