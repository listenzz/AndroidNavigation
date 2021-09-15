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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.navigation.BaseFragment;
import com.navigation.R;
import com.navigation.androidx.AwesomeToolbar;
import com.navigation.androidx.BarStyle;
import com.navigation.androidx.Style;


/**
 * Created by listen on 2018/2/2.
 */

public class CustomSystemUIFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener{

    Toolbar toolbar;

    TextView textView;

    @Override
    protected AwesomeToolbar onCreateAwesomeToolbar(View parent) {
        return null; // 自定义 Toolbar
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_custom_system_ui, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        appendStatusBarPadding(toolbar);
        textView = root.findViewById(R.id.hint);

        ((CheckBox)root.findViewById(R.id.tinting)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.dark)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.hidden)).setOnCheckedChangeListener(this);

        ((CheckBox)root.findViewById(R.id.navigation_color)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.navigation_hidden)).setOnCheckedChangeListener(this);
        ((CheckBox)root.findViewById(R.id.navigation_translucent)).setOnCheckedChangeListener(this);

        ((CheckBox)root.findViewById(R.id.display_cutout)).setOnCheckedChangeListener(this);

        return root;
    }

    @Override
    public void appendStatusBarPadding(View view) {
        if (!isInDialog()) {
            super.appendStatusBarPadding(view);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar.setNavigationIcon(style.getBackIcon());
        toolbar.setNavigationOnClickListener(v -> requireNavigationFragment().popFragment());
    }

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        super.onCustomStyle(style);
        style.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        textView.setText("");
        switch (buttonView.getId()) {
            case R.id.tinting:
                int statusBarColor = isChecked ? Color.MAGENTA : Color.TRANSPARENT;
                style.setStatusBarColor(statusBarColor);
                setNeedsStatusBarAppearanceUpdate();
                break;
            case R.id.dark: // 深色状态栏 6.0 以上生效
                BarStyle barStyle = isChecked ? BarStyle.DarkContent : BarStyle.LightContent;
                style.setStatusBarStyle(barStyle);
                setNeedsStatusBarAppearanceUpdate();

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    textView.setText("只有在 6.0 以上系统才能看到效果");
                }
                break;
            case R.id.hidden:
                style.setStatusBarHidden(isChecked);
                setNeedsStatusBarAppearanceUpdate();
                break;

            case R.id.navigation_color:
                navColorChecked = isChecked;
                setNeedsNavigationBarAppearanceUpdate();
                break;
            case R.id.navigation_translucent:
                navTranslucentChecked = isChecked;
                setNeedsNavigationBarAppearanceUpdate();
                break;
            case R.id.navigation_hidden:
                navHiddenChecked = isChecked;
                setNeedsNavigationBarAppearanceUpdate();
                break;

            case R.id.display_cutout:
                style.setDisplayCutoutWhenLandscape(isChecked);
                setNeedsStatusBarAppearanceUpdate();

                textView.setText("旋转屏幕看看");
                break;
        }
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

}
