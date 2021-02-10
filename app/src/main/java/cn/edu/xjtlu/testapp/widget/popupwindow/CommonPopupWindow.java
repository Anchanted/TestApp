package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.LayoutRes;

import cn.edu.xjtlu.testapp.R;

public abstract class CommonPopupWindow extends PopupWindow{
    protected View contentView;

    public CommonPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        this.contentView = contentView;
//        initWindow();
    }

    protected abstract void initView();
    protected abstract void initListener();
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void initWindow() {
//        setBackgroundDrawable(contentView.getContext().getDrawable(R.drawable.function_button));
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        setOutsideTouchable(true);
//        setFocusable(true);
//        setTouchable(true);
    }
}
