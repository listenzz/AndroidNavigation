package com.navigation.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

import me.listenzz.navigation.AwesomeFragment;

/**
 * Created by Listen on 2018/2/2.
 */
public class TopDialogFragment extends AwesomeFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 在 xml 中通过  android:layout_gravity="" 调整位置即可，注意布局的高度不应为 match_parent
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }
}
