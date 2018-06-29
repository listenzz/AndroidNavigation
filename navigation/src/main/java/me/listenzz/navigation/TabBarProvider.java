package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

/**
 * Created by Listen on 2018/06/29.
 */
public interface TabBarProvider {

    View onCreateTabBar(@NonNull List<TabBarItem> tabBarItems, @NonNull TabBarFragment tabBarFragment, @Nullable Bundle savedInstanceState);

    void onDestroyTabBar();

    void onSaveInstanceState(Bundle outState);

    void setSelectedIndex(int index);

}
