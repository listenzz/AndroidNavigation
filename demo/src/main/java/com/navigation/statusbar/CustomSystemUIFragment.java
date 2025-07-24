package com.navigation.statusbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;
import com.navigation.androidx.Style;


public class CustomSystemUIFragment extends BaseFragment {

    AwesomeToolbar toolbar;
    TextView textView;

    @Override
    protected AwesomeToolbar onCreateToolbar(View parent) {
        return toolbar; // 自定义 Toolbar
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_custom_system_ui, container, false);
        toolbar = root.findViewById(R.id.toolbar);

        textView = root.findViewById(R.id.hint);

        ((CheckBox)root.findViewById(R.id.tinting)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            statusBarColor = isChecked ? Color.MAGENTA : Color.TRANSPARENT;
            setNeedsStatusBarAppearanceUpdate();
        });
        ((CheckBox)root.findViewById(R.id.dark)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            statusBarStyle = isChecked ? BarStyle.DarkContent : BarStyle.LightContent;
            setNeedsStatusBarAppearanceUpdate();
        });
        ((CheckBox)root.findViewById(R.id.hidden)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            statusBarHidden = isChecked;
            setNeedsStatusBarAppearanceUpdate();
        });

        ((CheckBox)root.findViewById(R.id.navigation_color)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            navColorChecked = isChecked;
            setNeedsNavigationBarAppearanceUpdate();
        });
        ((CheckBox)root.findViewById(R.id.navigation_hidden)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            navHiddenChecked = isChecked;
            setNeedsNavigationBarAppearanceUpdate();
        });
        ((CheckBox)root.findViewById(R.id.navigation_translucent)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            navTranslucentChecked = isChecked;
            setNeedsNavigationBarAppearanceUpdate();
            getWindow().getDecorView().requestApplyInsets();
        });

        ((CheckBox)root.findViewById(R.id.display_cutout)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            mStyle.setDisplayCutoutWhenLandscape(isChecked);
            setNeedsLayoutInDisplayCutoutModeUpdate();
            textView.setText("旋转屏幕看看");
        });

        return root;
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setToolbarBackgroundColor(Color.TRANSPARENT);
    }

    boolean navColorChecked;
    boolean navTranslucentChecked;

    @Override
    protected int preferredNavigationBarColor() {
        if (navColorChecked && navTranslucentChecked) {
            return ColorUtils.setAlphaComponent(Color.BLUE, 100);
        }

        if (navColorChecked) {
            return Color.BLUE;
        }

        if (navTranslucentChecked) {
            return Color.TRANSPARENT;
        }

        return super.preferredNavigationBarColor();
    }

    boolean navHiddenChecked;

    @Override
    protected boolean preferredNavigationBarHidden() {
        return navHiddenChecked;
    }

    BarStyle statusBarStyle = BarStyle.LightContent;

    @NonNull
    @Override
    protected BarStyle preferredStatusBarStyle() {
        return statusBarStyle;
    }

    int statusBarColor = Color.TRANSPARENT;

    @Override
    protected int preferredStatusBarColor() {
        return statusBarColor;
    }

    boolean statusBarHidden = false;

    @Override
    protected boolean preferredStatusBarHidden() {
        return statusBarHidden;
    }
}
