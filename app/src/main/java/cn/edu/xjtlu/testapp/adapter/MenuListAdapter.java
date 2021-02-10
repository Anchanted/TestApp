package cn.edu.xjtlu.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;

import java.util.List;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.util.LogUtil;

public class MenuListAdapter extends BaseAdapter {
    private List<View> mList;

    public MenuListAdapter(@NonNull Context context, @NonNull List<View> objects) {
        this.mList = objects;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return (View) getItem(position);
//        MenuListAdapter.ViewHolder viewHolder;
//        if (convertView == null) {
//            convertView = (View) getItem(position);
//            viewHolder = new ViewHolder();
//            viewHolder.view = convertView;
//
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//        viewHolder.view = (View) getItem(position);
//
//        return convertView;
    }

    class ViewHolder {
        View view;
    }
}
