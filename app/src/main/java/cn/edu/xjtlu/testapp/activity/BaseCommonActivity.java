package cn.edu.xjtlu.testapp.activity;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import butterknife.ButterKnife;
import cn.edu.xjtlu.testapp.util.PreferencesUtil;

public class BaseCommonActivity extends BaseActivity{
    protected PreferencesUtil sp;

    @Override
    protected void initViews() {
        super.initViews();

        if (isBindView()) {
            bindView();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        sp = PreferencesUtil.getInstance(getMainActivity());
    }

    protected void startActivity(Class<?> clazz) {
        startActivity(new Intent(getMainActivity(), clazz));
    }

    protected void startActivityAndFinish(Class<?> clazz) {
        startActivity(new Intent(getMainActivity(), clazz));
        finish();
    }

    protected void startActivityAndFinish(Intent intent) {
        startActivity(intent);
        finish();
    }

    protected BaseCommonActivity getMainActivity() {
        return this;
    }

    protected void fullScreen() {
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(options);
        }
    }

    protected void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void bindView() {
        ButterKnife.bind(this);
    }

    protected boolean isBindView() {
        return true;
    }
}
