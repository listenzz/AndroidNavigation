package com.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navigation.androidx.BarStyle;
import com.navigation.androidx.DrawerFragment;
import com.navigation.androidx.StackFragment;
import com.navigation.androidx.Style;
import com.navigation.androidx.TabBar;
import com.navigation.androidx.TabBarFragment;
import com.navigation.androidx.TabBarItem;
import com.navigation.androidx.ToolbarButtonItem;

public class TestNavigationFragment extends BaseFragment {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private static final int REQUEST_CODE = 1;

    TextView resultText;

    EditText resultEditText;

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setStatusBarStyle(BarStyle.DarkContent);
    }

    @Override
    public void appendStatusBarPadding(View view) {
        if (getDialogAwesomeFragment() == null) {
            super.appendStatusBarPadding(view);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_test, container, false);

        resultText = root.findViewById(R.id.result_text);
        resultEditText = root.findViewById(R.id.result);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.present).setOnClickListener(v -> {
            StackFragment stackFragment = new StackFragment();
            stackFragment.setRootFragment(new TestNavigationFragment());
            presentFragment(stackFragment, REQUEST_CODE);
        });

        root.findViewById(R.id.dismiss).setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("text", resultEditText.getText().toString());
            setResult(Activity.RESULT_OK, result);
            dismissFragment();
        });

        root.findViewById(R.id.push).setOnClickListener(v -> {
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                stackFragment.pushFragment(new TestNavigationFragment());
            }
        });

        root.findViewById(R.id.pop).setOnClickListener(v -> {
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                Bundle result = new Bundle();
                result.putString("text", resultEditText.getText().toString());
                setResult(Activity.RESULT_OK, result);
                stackFragment.popFragment();
            }
        });

        root.findViewById(R.id.pop_to_root).setOnClickListener(v -> {
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                Bundle result = new Bundle();
                result.putString("text", resultEditText.getText().toString());
                setResult(Activity.RESULT_OK, result);
                stackFragment.popToRootFragment();
            }
        });

        root.findViewById(R.id.redirect).setOnClickListener(v -> {
            StackFragment stackFragment = getStackFragment();
            if (stackFragment != null) {
                stackFragment.redirectToFragment(new TestNavigationFragment());
            }
        });

        root.findViewById(R.id.show_badge).setOnClickListener(view -> {
            TabBarFragment tabBarFragment = getTabBarFragment();
            if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                TabBar tabBar = tabBarFragment.getTabBar();
                TabBarItem tabBarItem0 = tabBar.getTabBarItem(0);
                tabBarItem0.badgeText = "88";
                TabBarItem tabBarItem1 = tabBar.getTabBarItem(1);
                tabBarItem1.showDotBadge = true;
                tabBar.renderAllTabView();
            }
        });

        root.findViewById(R.id.hide_badge).setOnClickListener(view -> {
            TabBarFragment tabBarFragment = getTabBarFragment();
            if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                TabBar tabBar = tabBarFragment.getTabBar();
                TabBarItem tabBarItem0 = tabBar.getTabBarItem(0);
                tabBarItem0.badgeText = "";
                TabBarItem tabBarItem1 = tabBar.getTabBarItem(1);
                tabBarItem1.showDotBadge = false;
                tabBar.renderAllTabView();
            }
        });

        if (isStackRoot()) {
            root.findViewById(R.id.pop).setEnabled(false);
            root.findViewById(R.id.pop_to_root).setEnabled(false);
        }

        if (getPresentingFragment() == null) {
            root.findViewById(R.id.dismiss).setEnabled(false);
        }

        if (getDialogAwesomeFragment() != null) {
            root.findViewById(R.id.present).setEnabled(false);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("导航");
        setButton();
    }

    private void setButton() {
        if (!isStackRoot()) {
            return;
        }

        if (getPresentingFragment() == null) {
            setMenuButton();
            return;
        }

        setCloseButton();
    }

    private void setCloseButton() {
        ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
        builder.title("关闭").listener(v -> dismissFragment());
        setLeftBarButtonItem(builder.build());
    }

    private void setMenuButton() {
        String iconUri = "font://FontAwesome/" + fromCharCode(61641) + "/24";
        ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
        builder.icon(iconUri).listener(v -> {
            DrawerFragment drawerFragment = getDrawerFragment();
            if (drawerFragment != null) {
                drawerFragment.toggleMenu();
            }
        });
        setLeftBarButtonItem(builder.build());
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if ((requestCode != REQUEST_CODE)) {
            handlePopResult(resultCode, data);
            return;
        }

        handleModalResult(resultCode, data);
    }

    private void handlePopResult(int resultCode, @Nullable Bundle data) {
        if (resultCode == 0 || data == null) {
            return;
        }
        String text = data.getString("text", "");
        resultText.setText("pop result：" + text);
    }

    private void handleModalResult(int resultCode, @Nullable Bundle data) {
        if ((resultCode == 0 || data == null)) {
            resultText.setText("ACTION CANCEL");
            return;
        }

        String text = data.getString("text", "");
        resultText.setText("present result：" + text);
    }
}
