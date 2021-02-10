package cn.edu.xjtlu.testapp;

import android.app.Application;
import android.content.Context;
import android.view.View;

import androidx.multidex.MultiDex;

import cn.edu.xjtlu.testapp.util.NetworkUtil;
import cn.edu.xjtlu.testapp.util.ToastUtil;
import es.dmoral.toasty.Toasty;

public class AppContext extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        Toasty.Config.getInstance().apply();

        ToastUtil.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getContext() {
        return context;
    }
}
