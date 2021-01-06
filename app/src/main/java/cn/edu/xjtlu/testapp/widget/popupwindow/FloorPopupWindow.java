package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.LayoutRes;

import cn.edu.xjtlu.testapp.R;

public class FloorPopupWindow extends CommonPopupWindow{
    private BaseAdapter adapter;
    private AdapterView.OnItemClickListener listener;

    public FloorPopupWindow(Context context, @LayoutRes int layoutRes, int width, int height, BaseAdapter adapter, AdapterView.OnItemClickListener listener) {
        super(context, layoutRes, width, height);
        this.adapter = adapter;
        this.listener = listener;

        initView();
        initWindow();
    }

    @Override
    protected void initView() {
        ListView listView = contentView.findViewById(R.id.lv_floor);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);
    }
}
