package com.music.vkm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by mascot on 28.08.2017.
 */

public class Cookie implements Serializable {
    private static final long serialVersionUID = 1L;

    int i = 0, itmp;
    String[] cookarr = new String[12];

    public Cookie() {
        Arrays.fill(cookarr, "");
    }

    public Cookie(int tmp, String tmpstr) {
        cookarr[tmp] = tmpstr;
    }

    void addCookie(String tmpstr) {
        cookarr[i] = tmpstr;
        i++;
    }

    String getCookieinLine() {
        StringBuilder tmp = new StringBuilder();
        for (itmp = 0; itmp < i; itmp++) {
            tmp.append(cookarr[itmp]).append(";");
        }
        return tmp.toString();
    }

    void setCookie(int tmp, String tmpstr) {
        cookarr[tmp] = tmpstr;
    }
}
