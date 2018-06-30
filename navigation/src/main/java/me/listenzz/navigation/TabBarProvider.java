package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

/**
 * Created by Listen on 2018/06/29.
 * <p>
 * 注意：实现类的构造函数必须是无参构造函数，因为当 Activity 销毁后重建，
 * TabBarFragment 会利用此实现类的无参构造函数构建其实例
 * </p>
 */
public interface TabBarProvider {

    View onCreateTabBar(@NonNull List<TabBarItem> tabBarItems, @NonNull TabBarFragment tabBarFragment, @Nullable Bundle savedInstanceState);

    void onDestroyTabBar();

    void onSaveInstanceState(Bundle outState);

    void setSelectedIndex(int index);

}
