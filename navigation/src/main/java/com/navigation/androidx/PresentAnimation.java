package com.navigation.androidx;

import androidx.annotation.AnimRes;

/**
 * Created by Listen on 2017/11/20.
 */

public enum PresentAnimation {

    Push(R.anim.nav_push_enter, R.anim.nav_push_exit, R.anim.nav_pop_enter, R.anim.nav_pop_exit),
    Redirect(R.anim.nav_push_enter,  R.anim.nav_push_exit,  R.anim.nav_push_enter,  R.anim.nav_push_exit),
    Present(R.anim.nav_present_enter, R.anim.nav_present_exit, R.anim.nav_dismiss_enter, R.anim.nav_dismiss_exit),
    DelayShort(R.anim.nav_delay_short, R.anim.nav_delay_short, R.anim.nav_delay_short, R.anim.nav_delay_short),
    Fade(R.anim.nav_fade_in, R.anim.nav_fade_out, R.anim.nav_fade_in, R.anim.nav_fade_out),
    FadeShort(R.anim.nav_fade_in_short, R.anim.nav_fade_out_short, R.anim.nav_fade_in_short, R.anim.nav_fade_out_short),
    None(R.anim.nav_none, R.anim.nav_none, R.anim.nav_none, R.anim.nav_none);

    @AnimRes
    int enter;
    @AnimRes
    int exit;
    @AnimRes
    int popEnter;
    @AnimRes
    int popExit;

    PresentAnimation(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

}
