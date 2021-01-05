package cn.edu.xjtlu.testapp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

import cn.edu.xjtlu.testapp.MainActivity;
import cn.edu.xjtlu.testapp.util.LogUtil;

public class SplashActivity extends BaseCommonActivity{
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivityAndFinish(MainActivity.class);
    }
}
