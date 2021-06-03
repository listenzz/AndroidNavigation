package com.navigation.androidx;

import androidx.annotation.AnimRes;

/**
 * Created by Listen on 2017/11/20.
 */

public class TransitionAnimation {

    public final static TransitionAnimation Push = new TransitionAnimation(R.anim.nav_push_enter, R.anim.nav_push_exit, R.anim.nav_pop_enter, R.anim.nav_pop_exit);
    public final static TransitionAnimation Redirect = new TransitionAnimation(R.anim.nav_push_enter,  R.anim.nav_push_exit,  R.anim.nav_push_enter,  R.anim.nav_push_exit);
    public final static TransitionAnimation Present = new TransitionAnimation(R.anim.nav_present_enter, R.anim.nav_present_exit, R.anim.nav_dismiss_enter, R.anim.nav_dismiss_exit);
    public final static TransitionAnimation DelayShort = new TransitionAnimation(R.anim.nav_delay_short, R.anim.nav_delay_short, R.anim.nav_delay_short, R.anim.nav_delay_short);
    public final static TransitionAnimation Fade = new TransitionAnimation(R.anim.nav_fade_in, R.anim.nav_fade_out, R.anim.nav_fade_in, R.anim.nav_fade_out);
    public final static TransitionAnimation FadeShort = new TransitionAnimation(R.anim.nav_fade_in_short, R.anim.nav_fade_out_short, R.anim.nav_fade_in_short, R.anim.nav_fade_out_short);
    public final static TransitionAnimation None = new TransitionAnimation(R.anim.nav_none, R.anim.nav_none, R.anim.nav_none, R.anim.nav_none);

    @AnimRes
    final int enter;
    @AnimRes
    final int exit;
    @AnimRes
    final int popEnter;
    @AnimRes
    final int popExit;

    TransitionAnimation(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

}
