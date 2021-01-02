package cn.edu.xjtlu.testapp.util;

import android.app.Activity;
import android.app.ProgressDialog;

import cn.edu.xjtlu.testapp.R;

public class LoadingUtil {
    private static ProgressDialog progressDialog;

    public static void showLoading(Activity activity) {
        showLoading(activity, activity.getString(R.string.loading));
    }

    public static void showLoading(Activity activity, String message) {
        if (activity == null || activity.isFinishing()) return;

        if (progressDialog != null) {
            return;
        }

        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("提示");
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public static void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
