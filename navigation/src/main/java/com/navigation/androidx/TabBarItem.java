package com.navigation.androidx;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;


/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarItem implements Parcelable {

    @NonNull
    public String title;
    public String iconUri;
    public String unselectedIconUri;
    @DrawableRes
    public int iconRes;
    @DrawableRes
    public int unselectedIconRes;
    public String badgeText;
    public boolean showDotBadge;

    public TabBarItem(@DrawableRes int iconRes, @DrawableRes int unselectedIconRes, @NonNull String title) {
        this.title = title;
        this.iconRes = iconRes;
        this.unselectedIconRes = unselectedIconRes;
        this.iconUri = null;
        this.unselectedIconUri = null;
    }

    public TabBarItem(@DrawableRes int iconRes, @NonNull String title) {
        this.title = title;
        this.iconRes = iconRes;
        this.unselectedIconRes = -1;
        this.iconUri = null;
        this.unselectedIconUri = null;
    }

    public TabBarItem(@NonNull String iconUri, @NonNull String unselectedIconUri, @NonNull String title) {
        this.title = title;
        this.iconRes = -1;
        this.unselectedIconRes = -1;
        this.iconUri = iconUri;
        this.unselectedIconUri = unselectedIconUri;
    }

    public TabBarItem(@NonNull String iconUri, @NonNull String title) {
        this.title = title;
        this.iconRes = -1;
        this.unselectedIconRes = -1;
        this.iconUri = iconUri;
        this.unselectedIconUri = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.iconUri);
        dest.writeString(this.unselectedIconUri);
        dest.writeInt(this.iconRes);
        dest.writeInt(this.unselectedIconRes);
    }

    protected TabBarItem(Parcel in) {
        String title = in.readString();
        assert title != null;
        this.title = title;
        this.iconUri = in.readString();
        this.unselectedIconUri = in.readString();
        this.iconRes = in.readInt();
        this.unselectedIconRes = in.readInt();
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
