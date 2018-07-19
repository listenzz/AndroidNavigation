package me.listenzz.navigation;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

/**
 * Created by Listen on 2018/1/11.
 */

public class TabBarItem implements Parcelable {

    public String title;
    public String iconUri;
    public String selectedIconUri;
    @DrawableRes
    public int iconRes = -1;
    @DrawableRes
    public int selectedIconRes = -1;

    public TabBarItem(@DrawableRes int iconRes, @DrawableRes int selectedIconRes, String title) {
        this.iconRes = iconRes;
        this.selectedIconRes = selectedIconRes;
        this.title = title;
    }

    public TabBarItem(@DrawableRes int iconRes, String title) {
        this.iconRes = iconRes;
        this.title = title;
    }

    public TabBarItem(String iconUri, String selectedIconUri, String title) {
        this.iconUri = iconUri;
        this.selectedIconUri = selectedIconUri;
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
        dest.writeString(this.selectedIconUri);
        dest.writeInt(this.iconRes);
        dest.writeInt(this.selectedIconRes);
    }

    protected TabBarItem(Parcel in) {
        this.title = in.readString();
        this.iconUri = in.readString();
        this.selectedIconUri = in.readString();
        this.iconRes = in.readInt();
        this.selectedIconRes = in.readInt();
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
