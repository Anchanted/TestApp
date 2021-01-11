package cn.edu.xjtlu.testapp.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import es.dmoral.toasty.Toasty;

public class ToastUtil {
    private static Context context;

    public static void init(Context context) {
        ToastUtil.context = context;
    }

    public static void shortToastError(@StringRes int id) {
        Toasty.error(context, id, Toasty.LENGTH_SHORT).show();
    }

    public static void shortToastError(String message) {
        Toasty.error(context, message, Toasty.LENGTH_SHORT).show();
    }

    public static void longToastError(@StringRes int id) {
        Toasty.error(context, id, Toasty.LENGTH_LONG).show();
    }

    public static void longToastError(String message) {
        Toasty.error(context, message, Toasty.LENGTH_LONG).show();
    }

    public static void shortToastSuccess(@StringRes int id) {
        Toasty.success(context, id, Toasty.LENGTH_SHORT).show();
    }

    public static void shortToastSuccess(String message) {
        Toasty.success(context, message, Toasty.LENGTH_SHORT).show();
    }

    public static void longToastSuccess(@StringRes int id) {
        Toasty.success(context, id, Toasty.LENGTH_LONG).show();
    }
}
