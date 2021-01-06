package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.LayoutRes;

public abstract class CommonPopupWindow {
    private int width;
    private int height;
    protected Context context;
    protected View contentView;
    protected PopupWindow mInstance;

    public CommonPopupWindow(Context context, @LayoutRes int layoutRes, int width, int height) {
        this.width = width;
        this.height = height;
        this.context = context.getApplicationContext();
        this.contentView = LayoutInflater.from(context).inflate(layoutRes, null, false);
//        initView();
//        initListener();
//        initWindow();
    }

    public View getContentView() { return contentView; }
    public PopupWindow getPopupWindow() { return mInstance; }

    protected abstract void initView();
//    protected abstract void initListener();
    protected void initWindow() {
        mInstance = new PopupWindow(contentView, width, height);
        mInstance.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mInstance.setOutsideTouchable(true);
//        mInstance.setFocusable(true);
        mInstance.setTouchable(true);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        mInstance.showAsDropDown(anchor, xoff, yoff);
    }
    public void showAtLocation(View parent, int gravity, int x, int y) {
        mInstance.showAtLocation(parent, gravity, x, y);
    }
    public void dismiss() {
        mInstance.dismiss();
    }
}
