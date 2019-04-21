package com.music.vkm.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.apache.commons.lang3.StringEscapeUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


/**
 * Created by halez on 22.05.2017.
 */

public class AudioUtil {

    public static String removeCharAt(String s, int pos) {
        return s.substring(0, pos) + s.substring(pos + 1);
    }


    public static String pars(String T_, String ForS, String _T) {
        String result = "";
        int a, b;

        if (ForS.length() == -1 || ForS.length() == 0) {
            result = "Error in parsing";
        } else {
            a = ForS.indexOf(T_);

            if ((a + (T_.length()) > ForS.length())) {
                return "Error in parsing";
            }
            ForS = ForS.substring(a + (T_.length()));

            b = ForS.indexOf(_T);
            if (b != -1) {
                result = ForS.substring(0, b);
            } else result = "Error in parsing";

        }
        // result = normalizeString(result);
        return result;
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String decodeUrl() {

        String r = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=";

        String code = "var r = \"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=\"\n" +
                "      , l = {\n" +
                "        v: function(t) {\n" +
                "            return t.split(\"\").reverse().join(\"\")\n" +
                "        },\n" +
                "        r: function(t, e) {\n" +
                "            t = t.split(\"\");\n" +
                "            for (var i, o = r + r, a = t.length; a--; )\n" +
                "                i = o.indexOf(t[a]),\n" +
                "                ~i && (t[a] = o.substr(i - e, 1));\n" +
                "            return t.join(\"\")\n" +
                "        },\n" +
                "        s: function(t, e) {\n" +
                "            var i = t.length;\n" +
                "            if (i) {\n" +
                "                var o = s(t, e)\n" +
                "                  , a = 0;\n" +
                "                for (t = t.split(\"\"); ++a < i; )\n" +
                "                    t[a] = t.splice(o[i - 1 - a], 1, t[a])[0];\n" +
                "                t = t.join(\"\")\n" +
                "            }\n" +
                "            return t\n" +
                "        },\n" +
                "        x: function(t, e) {\n" +
                "            var i = [];\n" +
                "            return e = e.charCodeAt(0),\n" +
                "            each(t.split(\"\"), function(t, o) {\n" +
                "                i.push(String.fromCharCode(o.charCodeAt(0) ^ e))\n" +
                "            }),\n" +
                "            i.join(\"\")\n" +
                "        }\n" +
                "    }\n" +
                "function i() {\n" +
                "        return \"\"\n" +
                "    }\n" +
                "    function o(t) {\n" +
                "        if (!i() && ~t.indexOf(\"audio_api_unavailable\")) {\n" +
                "            var e = t.split(\"?extra=\")[1].split(\"#\")\n" +
                "              , o = \"\" === e[1] ? \"\" : a(e[1]);\n" +
                "            if (e = a(e[0]),\n" +
                "            \"string\" != typeof o || !e)\n" +
                "                return t;\n" +
                "            o = o ? o.split(String.fromCharCode(9)) : [];\n" +
                "            for (var s, r, n = o.length; n--; ) {\n" +
                "                if (r = o[n].split(String.fromCharCode(11)),\n" +
                "                s = r.splice(0, 1, e)[0],\n" +
                "                !l[s])\n" +
                "                    return t;\n" +
                "                e = l[s].apply(null, r)\n" +
                "            }\n" +
                "            if (e && \"http\" === e.substr(0, 4))\n" +
                "                return e\n" +
                "        }\n" +
                "        return t\n" +
                "    }\n" +
                "    function a(t) {\n" +
                "        if (!t || t.length % 4 == 1)\n" +
                "            return !1;\n" +
                "        for (var e, i, o = 0, a = 0, s = \"\"; i = t.charAt(a++); )\n" +
                "            i = r.indexOf(i),\n" +
                "            ~i && (e = o % 4 ? 64 * e + i : i,\n" +
                "            o++ % 4) && (s += String.fromCharCode(255 & e >> (-2 * o & 6)));\n" +
                "        return s\n" +
                "    }\n" +
                "    function s(t, e) {\n" +
                "        var i = t.length\n" +
                "          , o = [];\n" +
                "        if (i) {\n" +
                "            var a = i;\n" +
                "            for (e = Math.abs(e); a--; )\n" +
                "                o[a] = (e += e * (a + i) / e) % i | 0\n" +
                "        }\n" +
                "        return o\n" +
                "}\n";


        return "";
    }

    public static String millisecondsToTime(int millis) {
        if (millis < 0) {
            millis = 0;
        }
        long min = TimeUnit.MILLISECONDS.toMinutes(millis);
        long sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static String normalizeString(String str) {

        str = StringEscapeUtils.unescapeHtml4(str);

        return str;
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}