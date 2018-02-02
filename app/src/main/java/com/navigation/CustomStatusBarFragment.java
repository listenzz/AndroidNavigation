package com.navigation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.navigation.fragment.AwesomeFragment;
import com.navigation.fragment.BarStyle;

/**
 * Created by listen on 2018/2/2.
 */

public class CustomStatusBarFragment extends AwesomeFragment implements CompoundButton.OnCheckedChangeListener{

    Toolbar toolbar;

    @Override
    protected Toolbar onCreateToolbar(View parent) {
        return null; // 自定义 Toolbar
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_custom_statusbar, container, false);
        toolbar = root.findViewById(R.id.toolbar);

        ((CheckBox)root.findViewById(R.id.insets)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.tinting)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.dark)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.hidden)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.adjust)).setOnCheckedChangeListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setStatusBarTranslucent(true);
    }

    int statusBarColor = Color.MAGENTA;
    BarStyle statusBarStyle = BarStyle.LightContent;
    boolean statusBarHidden = false;

    @Override
    protected int preferredStatusBarColor() {
        return statusBarColor;
    }

    @Override
    protected BarStyle preferredStatusBarStyle() {
        return statusBarStyle;
    }

    @Override
    protected boolean preferredStatusBarHidden() {
        return statusBarHidden;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.insets: // 状态栏沉浸 5.0 以上生效
                // 慎重，会影响整个 Activity
                setStatusBarTranslucent(isChecked);
                getWindow().getDecorView().requestLayout();
                break;
            case R.id.tinting: // 状态栏着色 5.0 以上生效
                statusBarColor = isChecked ? Color.MAGENTA : Color.TRANSPARENT;
                setNeedsStatusBarAppearanceUpdate();
                break;
            case R.id.dark: // 深色状态栏 6.0 以上生效
                statusBarStyle = isChecked ? BarStyle.DarkContent : BarStyle.LightContent;
                setNeedsStatusBarAppearanceUpdate();
                break;
            case R.id.hidden: // 隐藏状态栏 5.0 以上生效
                statusBarHidden = isChecked;
                setNeedsStatusBarAppearanceUpdate();
                break;
            case R.id.adjust: //调整 Toolbar 位置 5.0 以上生效
                if (isChecked) {
                    appendStatusBarPaddingAndHeight(toolbar, getToolbarHeight());
                } else {
                    removeStatusBarPaddingAndHeight(toolbar, getToolbarHeight());
                }
                break;
        }
    }

}
