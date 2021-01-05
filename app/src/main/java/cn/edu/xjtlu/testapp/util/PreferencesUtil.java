package cn.edu.xjtlu.testapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.prefs.Preferences;

public class PreferencesUtil {
    private static final String NAME = "xap";
    private static final String SHOW_GUIDE = "SHOW_GUIDE";
    private static PreferencesUtil instance;
    private final Context mContext;
    private final SharedPreferences preferences;

    private PreferencesUtil(Context context) {
        mContext = context.getApplicationContext();

        preferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static PreferencesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesUtil(context);
        }
        return instance;
    }

    public boolean isShowGuide() {
        return preferences.getBoolean(SHOW_GUIDE, true);
    }

    public void setShowGuide(boolean value) {
        putBoolean(SHOW_GUIDE, value);
    }

    private void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    private void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
}
