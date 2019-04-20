package com.music.vkm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mascot on 28.08.2017.
 */

public class cookie implements Parcelable {
    int i = 0, itmp;
    String[] cookarr = new String[12];

    cookie() {
        for (itmp = 0; itmp < 12; itmp++) {
            cookarr[itmp] = "";
        }
    }

    cookie(int tmp, String tmpstr) {
        cookarr[tmp] = tmpstr;
    }

    void addCookie(String tmpstr) {
        cookarr[i] = tmpstr;
        i++;
    }

    String getCookieinLine(){
        StringBuilder tmp = new StringBuilder();
        for (itmp = 0;itmp < i;itmp++){
            tmp.append(cookarr[itmp]).append(";");
        }
        return tmp.toString();
    }

    void setCookie(int tmp, String tmpstr) {
        cookarr[tmp] = tmpstr;
    }

    String getCookie(int tmp){
        return cookarr[tmp];
    }

    protected cookie(Parcel in) {
        i = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(i);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<cookie> CREATOR = new Parcelable.Creator<cookie>() {
        @Override
        public cookie createFromParcel(Parcel in) {
            return new cookie(in);
        }

        @Override
        public cookie[] newArray(int size) {
            return new cookie[size];
        }
    };
}
