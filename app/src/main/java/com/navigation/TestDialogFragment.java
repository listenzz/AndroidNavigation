package com.navigation;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.navigation.fragment.AwesomeFragment;

/**
 * Created by Listen on 2018/2/2.
 */
public class TestDialogFragment extends AwesomeFragment {

    protected Window mWindow;
    protected int mWidth;  //屏幕宽度
    protected int mHeight;  //屏幕高度

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Toolbar toolbar = getView().findViewById(R.id.toolbar);
        //appendStatusBarPaddingAndHeight(toolbar, getToolbarHeight());
        //setStatusBarTranslucent(true);
        setStatusBarColor(Color.TRANSPARENT, false);

        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(true);  //点击外部消失
        mWindow = dialog.getWindow();
        //测量宽高
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(dm);
            mWidth = dm.widthPixels;
            mHeight = dm.heightPixels;
        } else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mWidth = metrics.widthPixels;
            mHeight = metrics.heightPixels;
        }

       // mWindow.setGravity(Gravity.BOTTOM);
       // mWindow.setWindowAnimations(R.style.TopDialog);
       // mWindow.setLayout(mWidth, mHeight/2);

        WindowManager.LayoutParams layoutParams = mWindow.getAttributes();
        layoutParams.height = -2;
        layoutParams.width = -1;
        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        mWindow.setAttributes(layoutParams);

    }

}
