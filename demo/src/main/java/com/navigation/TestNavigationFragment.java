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

import me.listenzz.navigation.AwesomeFragment;
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

public class TestNavigationFragment extends AwesomeFragment {

    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private static final int REQUEST_CODE = 1;

    TextView resultText;

    EditText resultEditText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_test, container, false);

        resultText = root.findViewById(R.id.result_text);
        resultEditText = root.findViewById(R.id.result);

        TextView tagView = root.findViewById(R.id.tag);
        tagView.setText(getDebugTag());

        root.findViewById(R.id.present).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = new NavigationFragment();
                navigationFragment.setRootFragment(new TestNavigationFragment());
                presentFragment(navigationFragment, REQUEST_CODE);
            }
        });

        root.findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle result = new Bundle();
                result.putString("text", resultEditText.getText().toString());
                setResult(Activity.RESULT_OK, result);
                dismissFragment();
            }
        });

        root.findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = getNavigationFragment();
                if (navigationFragment != null) {
                    navigationFragment.pushFragment(new TestNavigationFragment());
                }
            }
        });

        root.findViewById(R.id.pop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = getNavigationFragment();
                if (navigationFragment != null) {
                    Bundle result = new Bundle();
                    result.putString("text", resultEditText.getText().toString());
                    setResult(Activity.RESULT_OK, result);
                    navigationFragment.popFragment();
                }
            }
        });

        root.findViewById(R.id.pop_to_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = getNavigationFragment();
                if (navigationFragment != null) {
                    Bundle result = new Bundle();
                    result.putString("text", resultEditText.getText().toString());
                    setResult(Activity.RESULT_OK, result);
                    navigationFragment.popToRootFragment();
                }
            }
        });

        root.findViewById(R.id.replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = getNavigationFragment();
                if (navigationFragment != null) {
                    navigationFragment.replaceFragment(new TestNavigationFragment());
                }
            }
        });

        root.findViewById(R.id.replace_to_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationFragment navigationFragment = getNavigationFragment();
                if (navigationFragment != null) {
                    navigationFragment.replaceToRootFragment(new TestNavigationFragment());
                }
            }
        });

        root.findViewById(R.id.show_badge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null) {
                    TabBar tabBar = tabBarFragment.getTabBar();
                    tabBar.setBadge(0, "12");
                    tabBar.setRedPoint(1, true);
                }
            }
        });

        root.findViewById(R.id.hide_badge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TabBarFragment tabBarFragment = getTabBarFragment();
                if (tabBarFragment != null) {
                    TabBar tabBar = tabBarFragment.getTabBar();
                    tabBar.setBadge(0, null);
                    tabBar.setRedPoint(1, false);
                }
            }
        });

        if (isNavigationRoot()) {
            root.findViewById(R.id.pop).setEnabled(false);
            root.findViewById(R.id.pop_to_root).setEnabled(false);
        }

        if (getPresentingFragment() == null) {
            root.findViewById(R.id.dismiss).setEnabled(false);
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
                setLeftBarButtonItem(new ToolbarButtonItem(iconUri, "Menu", true, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DrawerFragment drawerFragment = getDrawerFragment();
                        if (drawerFragment != null) {
                            drawerFragment.toggleMenu();
                        }
                    }
                }));
            } else {
                setLeftBarButtonItem(new ToolbarButtonItem(null, "关闭", true, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismissFragment();
                    }
                }));
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

    @Override
    protected void onCustomStyle(@NonNull Style style) {
        style.setStatusBarStyle(BarStyle.DarkContent);
    }

}
