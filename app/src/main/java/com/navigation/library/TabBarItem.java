package com.navigation.library;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarItem implements Parcelable {

    public String title;

    public String iconUri;

    public int iconRes = -1;

    public TabBarItem(@DrawableRes int iconRes, String title) {
        this.iconRes = iconRes;
        this.title = title;
    }

    public TabBarItem(String iconUri, String title) {
        this.iconUri = iconUri;
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.iconUri);
        dest.writeInt(this.iconRes);
    }

    protected TabBarItem(Parcel in) {
        this.title = in.readString();
        this.iconUri = in.readString();
        this.iconRes = in.readInt();
    }

    public static final Creator<TabBarItem> CREATOR = new Creator<TabBarItem>() {
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
