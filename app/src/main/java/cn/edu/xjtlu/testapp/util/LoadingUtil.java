package cn.edu.xjtlu.testapp.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.widget.LoadingDialog;

public class LoadingUtil {
    private static LoadingDialog mDialog;

    public static void showLoading(Activity activity) {
        showLoading(activity, null);
    }

    public static void showLoading(Activity activity, View.OnClickListener listener) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (activity == null || activity.isFinishing()) return;

        mDialog = new LoadingDialog(activity, listener);
//        progressDialog.setMessage(message);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void hideLoading() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.hide();
            mDialog.dismiss();
        }
        mDialog = null;
    }
}
