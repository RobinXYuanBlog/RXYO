package com.example.robinxyuan.rxyo.FontManager;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by robinxyuan on 2017/12/14.
 */

public class FontManager {

    public static final String ROOT = "fonts/",
            FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

}