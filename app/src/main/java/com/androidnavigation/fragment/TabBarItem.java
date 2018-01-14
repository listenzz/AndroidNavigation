package com.androidnavigation.fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarItem implements Parcelable {

    public String title;

    public @DrawableRes int icon;

    public TabBarItem(@DrawableRes int icon, String title) {
        this.icon = icon;
        this.title = title;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeInt(this.icon);
    }

    protected TabBarItem(Parcel in) {
        this.title = in.readString();
        this.icon = in.readInt();
    }

    public static final Parcelable.Creator<TabBarItem> CREATOR = new Parcelable.Creator<TabBarItem>() {
        @Override
        public TabBarItem createFromParcel(Parcel source) {
            return new TabBarItem(source);
        }

        @Override
        public TabBarItem[] newArray(int size) {
            return new TabBarItem[size];
        }
    };
}
