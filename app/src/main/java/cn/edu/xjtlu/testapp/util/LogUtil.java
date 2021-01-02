package cn.edu.xjtlu.testapp.util;

import android.util.Log;

import cn.edu.xjtlu.testapp.BuildConfig;

public class LogUtil {
    public static final boolean isDebug = BuildConfig.DEBUG;

    public static void d(String tag, String value) {
        if (isDebug) {
            Log.d(tag, value);
        }
    }
}
