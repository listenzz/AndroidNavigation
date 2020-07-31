package com.navigation.statusbar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.navigation.BaseFragment;
import com.navigation.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;


/**
 * Created by listen on 2018/2/2.
 */

public class CustomStatusBarFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener{

    Toolbar toolbar;

    TextView textView;

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return null; // 自定义 Toolbar
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_custom_statusbar, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        textView = root.findViewById(R.id.hint);

        ((CheckBox)root.findViewById(R.id.tinting)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.dark)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.hidden)).setOnCheckedChangeListener(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar.setNavigationIcon(style.getBackIcon());
        toolbar.setNavigationOnClickListener(v -> requireNavigationFragment().popFragment());
    }

    int statusBarColor = Color.MAGENTA;
    BarStyle statusBarStyle = BarStyle.LightContent;
    boolean statusBarHidden = false;

    @Override
    protected int preferredStatusBarColor() {
        return statusBarColor;
    }

    @NonNull
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
        textView.setText("");
        switch (buttonView.getId()) {
            case R.id.tinting:
                statusBarColor = isChecked ? Color.MAGENTA : Color.TRANSPARENT;
                setNeedsStatusBarAppearanceUpdate();
                if (statusBarHidden && isChecked) {
                    textView.setText("只有显示状态栏才能看到效果");
                }
                break;
            case R.id.dark: // 深色状态栏 6.0 以上生效
                statusBarStyle = isChecked ? BarStyle.DarkContent : BarStyle.LightContent;
                setNeedsStatusBarAppearanceUpdate();

                if (statusBarHidden && isChecked) {
                    textView.setText("只有显示状态栏才能看到效果");
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    textView.setText("只有在 6.0 以上系统才能看到效果");
                }
                break;
            case R.id.hidden:
                statusBarHidden = isChecked;
                setNeedsStatusBarAppearanceUpdate();
                break;
        }
    }

}
