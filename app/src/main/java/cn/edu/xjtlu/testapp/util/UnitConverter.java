package cn.edu.xjtlu.testapp.util;

import android.content.Context;

public class UnitConverter {
    public static float px2dp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dp2px(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
