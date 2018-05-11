package me.listenzz.navigation;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AnimRes;

public class Anim implements Parcelable {

    public static final Anim Push = new Anim(R.anim.nav_slide_in_right, R.anim.nav_slide_out_left, R.anim.nav_slide_in_left, R.anim.nav_slide_out_right);
    public static final Anim Modal = new Anim(R.anim.nav_slide_up, R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_slide_down);
    public static final Anim Fade = new Anim(R.anim.nav_fade_in, R.anim.nav_fade_out, R.anim.nav_fade_in, R.anim.nav_fade_out);
    public static final Anim Delay = new Anim(R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_delay, R.anim.nav_delay);
    public static final Anim None = new Anim(R.anim.nav_none, R.anim.nav_none, R.anim.nav_none, R.anim.nav_none);

    private int enter;
    private int exit;
    private int popEnter;
    private int popExit;

    Anim(@AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

    public int enter() {
        return enter;
    }

    public int exit() {
        return exit;
    }

    public int popEnter() {
        return popEnter;
    }

    public int popExit() {
        return popExit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.enter);
        dest.writeInt(this.exit);
        dest.writeInt(this.popEnter);
        dest.writeInt(this.popExit);
    }

    protected Anim(Parcel in) {
        this.enter = in.readInt();
        this.exit = in.readInt();
        this.popEnter = in.readInt();
        this.popExit = in.readInt();
    }

    public static final Creator<Anim> CREATOR = new Creator<Anim>() {
        @Override
        public Anim createFromParcel(Parcel source) {
            return new Anim(source);
        }

        @Override
        public Anim[] newArray(int size) {
            return new Anim[size];
        }
    };

}
