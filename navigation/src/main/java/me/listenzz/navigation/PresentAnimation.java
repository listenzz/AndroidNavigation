package me.listenzz.navigation;

import android.support.annotation.AnimRes;

/**
 * Created by Listen on 2017/11/20.
 */

public enum PresentAnimation {

    Push(R.anim.nav_slide_in_right, R.anim.nav_slide_out_left, R.anim.nav_slide_in_left, R.anim.nav_slide_out_right),
    Modal(R.anim.nav_slide_up, R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_slide_down),
    Delay(R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_delay),
    DelayShort(R.anim.nav_delay_short, R.anim.nav_delay_short,R.anim.nav_delay_short,R.anim.nav_delay_short),
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
