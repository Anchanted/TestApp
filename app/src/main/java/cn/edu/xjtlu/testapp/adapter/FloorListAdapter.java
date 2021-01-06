package cn.edu.xjtlu.testapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.domain.Floor;

public class FloorListAdapter extends BaseAdapter {
    private List<Floor> mList;
    private LayoutInflater inflater;
    private int selectedPosition = 0;

    public FloorListAdapter(@NonNull Context context, @NonNull List<Floor> objects) {
        this.mList = objects;
        this.inflater = LayoutInflater.from(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_dropdown_item, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.ctv = convertView.findViewById(R.id.ctv_floor_list_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Floor floor = (Floor) getItem(position);

        if (floor != null) {
            viewHolder.ctv.setText(floor.getName());
        }
        viewHolder.ctv.setChecked(position == selectedPosition);

        return convertView;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    class ViewHolder {
        CheckedTextView ctv;
    }
}
