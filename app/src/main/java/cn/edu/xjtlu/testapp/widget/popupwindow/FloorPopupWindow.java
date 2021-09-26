package cn.edu.xjtlu.testapp.widget.popupwindow;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.adapter.FloorListAdapter;

public class FloorPopupWindow extends CommonPopupWindow{
    @NonNull
    private final FloorListAdapter adapter;
    private final AdapterView.OnItemClickListener listener;
    private final ListView listView;

    public FloorPopupWindow(View contentView, int width, int height, @NotNull FloorListAdapter adapter, AdapterView.OnItemClickListener listener) {
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

    public FloorListAdapter getAdapter() {
        return adapter;
    }
}
