package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import cn.edu.xjtlu.testapp.R;

public class MenuPopupWindow extends CommonPopupWindow{
    private final BaseAdapter adapter;
    private final ListView listView;

    public MenuPopupWindow(View contentView, int width, int height, BaseAdapter adapter) {
        super(contentView, width, height);
        this.adapter = adapter;
        this.listView = contentView.findViewById(R.id.lv_floor);

        initView();
        initWindow();
    }

    @Override
    protected void initView() {
        listView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
    }
}
