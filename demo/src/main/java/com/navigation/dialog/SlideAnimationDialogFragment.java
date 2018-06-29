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
 * Created by listen on 2018/2/3.
 */

public class SlideAnimationDialogFragment extends AwesomeFragment {

    //    如果想要禁止默认的 slide 动画，可以重写 #getAnimationType
//    @Override
//    public AnimationType getAnimationType() {
//        return AnimationType.None;
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 当根布局设置了 android:layout_gravity="bottom"，会默认有一个 slide 动画
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
    }

}
