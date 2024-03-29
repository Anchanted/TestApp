package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.LayoutRes;

import cn.edu.xjtlu.testapp.R;

public class FloorPopupWindow extends CommonPopupWindow{
    private final BaseAdapter adapter;
    private final AdapterView.OnItemClickListener listener;
    private final ListView listView;

    public FloorPopupWindow(View contentView, int width, int height, BaseAdapter adapter, AdapterView.OnItemClickListener listener) {
        super(contentView, width, height);
        this.adapter = adapter;
        this.listener = listener;
        this.listView = contentView.findViewById(R.id.lv_floor);

        initView();
        initListener();
        initWindow();
    }

    @Override
    protected void initView() {
        listView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
        listView.setOnItemClickListener(listener);
    }
}
