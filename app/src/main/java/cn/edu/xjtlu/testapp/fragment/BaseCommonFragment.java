package cn.edu.xjtlu.testapp.fragment;

import android.content.Intent;

import cn.edu.xjtlu.testapp.activity.BaseCommonActivity;
import cn.edu.xjtlu.testapp.util.PreferencesUtil;

public abstract class BaseCommonFragment extends BaseFragment {
    protected PreferencesUtil sp;

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
        getMainActivity().finish();
    }

    protected BaseCommonActivity getMainActivity() {
        return (BaseCommonActivity) getActivity();
    }
}
