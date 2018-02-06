package com.navigation.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.navigation.R;
import com.navigation.library.AppUtils;
import com.navigation.library.AwesomeFragment;

/**
 * Created by listen on 2018/2/3.
 */

public class BottomSheetDialogFragment extends AwesomeFragment {


    @Override
    protected int preferredStatusBarColor() {
        return Color.TRANSPARENT;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getContext(), getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 开启沉浸式
        setContentUnderStatusBar(true);

        BottomSheetBehavior behavior = BottomSheetBehavior.from((View) getView().getParent());
        behavior.setHideable(true);
        behavior.setPeekHeight(AppUtils.dp2px(getContext(), 50));
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }
}
