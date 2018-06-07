package com.navigation.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import me.listenzz.navigation.AppUtils;
import me.listenzz.navigation.AwesomeFragment;


/**
 * Created by listen on 2018/2/3.
 */

public class BottomSheetDialogFragment extends AwesomeFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) getView().getParent());
        behavior.setHideable(true);
        behavior.setPeekHeight(AppUtils.dp2px(requireContext(), 50));
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

    }

}
