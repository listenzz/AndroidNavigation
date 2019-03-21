package com.navigation.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.navigation.R;

import me.listenzz.navigation.AnimationType;
import me.listenzz.navigation.AwesomeFragment;

/**
 * Created by Listen on 2018/2/2.
 */
public class CustomAnimationDialogFragment extends AwesomeFragment {

    @NonNull
    @Override
    public AnimationType getAnimationType() {
        return AnimationType.None;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 在 xml 中通过  android:layout_gravity="" 调整位置即可，注意布局的高度不应为 match_parent
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        mContentView = root.findViewById(R.id.dialog_content);
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                animateScaleIn();
            }
        });
        root.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(new AlertDialogFragment(), 1);
                dismissDialog();
            }
        });
    }

    private final static int mAnimationDuration = 300;
    private boolean mIsAnimating = false;
    private View mContentView;

    @Override
    public void dismissDialog() {
        if (mIsAnimating) {
            return;
        }
        animateScaleOut();
    }

    private void animateScaleOut() {
        if (mContentView == null) {
            return;
        }

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.0f, 1f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
        AlphaAnimation alpha = new AlphaAnimation(1, 0);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(mAnimationDuration);
        set.setFillAfter(true);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsAnimating = false;
                mContentView.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomAnimationDialogFragment.super.dismissDialog();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContentView.startAnimation(set);
    }

    private void animateScaleIn() {
        if (mContentView == null) {
            return;
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alpha);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(mAnimationDuration );
        set.setFillAfter(true);
        mContentView.startAnimation(set);
    }

}
