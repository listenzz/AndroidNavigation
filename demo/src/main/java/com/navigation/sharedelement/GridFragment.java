package com.navigation.sharedelement;

import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.navigation.BaseFragment;
import com.navigation.R;

/**
 * Displays a grid of pictures
 *
 * @author bherbst
 */
public class GridFragment extends BaseFragment implements KittenClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setAdapter(new KittenGridAdapter(6, this));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    @Override
    public void onKittenClicked(KittenViewHolder holder, int position) {
        int kittenNumber = (position % 6) + 1;

        DetailsFragment kittenDetails = DetailsFragment.newInstance(kittenNumber);

        // Note that we need the API version check here because the actual transition classes (e.g. Fade)
        // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
        // ARE available in the support library (though they don't do anything on API < 21)

            kittenDetails.setSharedElementEnterTransition(new DetailsTransition());
            kittenDetails.setEnterTransition(new Fade());
            setExitTransition(new Fade());
            kittenDetails.setSharedElementReturnTransition(new DetailsTransition());


        // 如果是通过异步回调的方式来触发转场，以下代码需要包裹在 scheduleTaskAtStarted 中

        // 将要显示的 Fragment 是 this 的兄弟
        getParentFragmentManager()
                .beginTransaction()
                // 很重要
                .setReorderingAllowed(true)
                // 因为开启了共享元素转场，就不要设置 FragmentTransaction#setTransition 或者 FragmentTransaction#setCustomAnimations 了
                .addSharedElement(holder.image, "kittenImage")
                // 在添加新的 Fragment 之前先隐藏旧的
                .hide(this)
                // 使当前 fragment 处于 pause 状态
                .setMaxLifecycle(this, Lifecycle.State.STARTED)
                // 使用具有三个参数的 add
                .add(R.id.navigation_content, kittenDetails, kittenDetails.getSceneId())
                // 因为 NavigationFragment 以栈的形式管理子 Fragment
                .addToBackStack(kittenDetails.getSceneId()/*important*/)
                // 使用 commit 而不是 commitAllowingStateLoss 是个好习惯
                .commit();
    }

}
