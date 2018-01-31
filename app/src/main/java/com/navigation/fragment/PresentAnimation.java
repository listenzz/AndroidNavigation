package com.navigation.fragment;

import android.support.annotation.AnimRes;

import com.navigation.R;

/**
 * Created by Listen on 2017/11/20.
 */

public enum PresentAnimation {

    Push(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right),
    Modal(R.anim.slide_up, R.anim.delay, R.anim.delay, R.anim.slide_down),
    Delay(R.anim.delay, R.anim.delay, R.anim.delay, R.anim.delay),
    Fade(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out),
    None(R.anim.node, R.anim.node, R.anim.node, R.anim.node);

    @AnimRes int enter;
    @AnimRes int exit;
    @AnimRes int popEnter;
    @AnimRes int popExit;

    PresentAnimation(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

}
