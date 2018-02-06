package me.listenzz.navigation.adapter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by Listen on 2018/2/6.
 */

public class SoftInputLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

    private View rootView;

    private int usableHeightPrevious;

    private final int initialHeight;

    public SoftInputLayoutListener(@NonNull View rootView) {
        this.rootView = rootView;
        this.initialHeight = rootView.getLayoutParams().height;
    }

    public int getInitialHeight() {
        return initialHeight;
    }

    @Override
    public void onGlobalLayout() {
        possiblyResizeChildOfContent();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight(rootView);
        if (usableHeightNow != usableHeightPrevious) {
            //如果两次高度不一致
            //将计算的可视高度设置成视图的高度
            rootView.getLayoutParams().height = usableHeightNow;
            rootView.requestLayout();//请求重新布局
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight(View view) {
        //计算视图可视高度
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        return r.bottom;
    }

}
