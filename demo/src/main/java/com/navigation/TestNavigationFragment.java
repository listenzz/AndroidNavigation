package com.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import me.listenzz.navigation.BarStyle;
import me.listenzz.navigation.DrawerFragment;
import me.listenzz.navigation.NavigationFragment;
import me.listenzz.navigation.Style;
import me.listenzz.navigation.TabBar;
import me.listenzz.navigation.TabBarFragment;
import me.listenzz.navigation.ToolbarButtonItem;


/**
 * Created by Listen on 2018/1/11.
 */

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
    public void appendStatusBarPadding(View view, int viewHeight) {
        if (!isInDialog()) {
            super.appendStatusBarPadding(view, viewHeight);
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
            NavigationFragment navigationFragment = new NavigationFragment();
            navigationFragment.setRootFragment(new TestNavigationFragment());
            presentFragment(navigationFragment, REQUEST_CODE);
        });

        root.findViewById(R.id.dismiss).setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("text", resultEditText.getText().toString());
            setResult(Activity.RESULT_OK, result);
            dismissFragment();
        });

        root.findViewById(R.id.push).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                navigationFragment.pushFragment(new TestNavigationFragment());
            }
        });

        root.findViewById(R.id.pop).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                Bundle result = new Bundle();
                result.putString("text", resultEditText.getText().toString());
                setResult(Activity.RESULT_OK, result);
                navigationFragment.popFragment();
            }
        });

        root.findViewById(R.id.pop_to_root).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                Bundle result = new Bundle();
                result.putString("text", resultEditText.getText().toString());
                setResult(Activity.RESULT_OK, result);
                navigationFragment.popToRootFragment();
            }
        });

        root.findViewById(R.id.replace).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                navigationFragment.replaceFragment(new TestNavigationFragment());
            }
        });

        root.findViewById(R.id.replace_to_root).setOnClickListener(v -> {
            NavigationFragment navigationFragment = getNavigationFragment();
            if (navigationFragment != null) {
                navigationFragment.replaceToRootFragment(new TestNavigationFragment());
            }
        });

        root.findViewById(R.id.show_badge).setOnClickListener(view -> {
            TabBarFragment tabBarFragment = getTabBarFragment();
            if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                TabBar tabBar = tabBarFragment.getTabBar();
                tabBar.showTextBadgeAtIndex(0, "88");
                tabBar.showDotBadgeAtIndex(1);
            }
        });

        root.findViewById(R.id.hide_badge).setOnClickListener(view -> {
            TabBarFragment tabBarFragment = getTabBarFragment();
            if (tabBarFragment != null && tabBarFragment.getTabBar() != null) {
                TabBar tabBar = tabBarFragment.getTabBar();
                tabBar.hideBadgeAtIndex(0);
                tabBar.hideBadgeAtIndex(1);
            }
        });

        if (isNavigationRoot()) {
            root.findViewById(R.id.pop).setEnabled(false);
            root.findViewById(R.id.pop_to_root).setEnabled(false);
        }

        if (getPresentingFragment() == null) {
            root.findViewById(R.id.dismiss).setEnabled(false);
        }

        if (isInDialog()) {
            root.findViewById(R.id.present).setEnabled(false);
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle("导航");

        if (isNavigationRoot()) {
            if (getPresentingFragment() == null) {
                String iconUri = "font://FontAwesome/" + fromCharCode(61641) + "/24";
                ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
                builder.icon(iconUri).listener(view -> {
                    DrawerFragment drawerFragment = getDrawerFragment();
                    if (drawerFragment != null) {
                        drawerFragment.toggleMenu();
                    }
                });
                setLeftBarButtonItem(builder.build());
            } else {
                ToolbarButtonItem.Builder builder = new ToolbarButtonItem.Builder();
                builder.title("关闭").listener(view -> dismissFragment());
                setLeftBarButtonItem(builder.build());
            }
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, @Nullable Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode != 0 && data != null) {
                String text = data.getString("text", "");
                resultText.setText("present result：" + text);
            } else {
                resultText.setText("ACTION CANCEL");
            }
        } else {
            if (resultCode != 0 && data != null) {
                String text = data.getString("text", "");
                resultText.setText("pop result：" + text);
            }
        }
    }

}
