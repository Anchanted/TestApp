package cn.edu.xjtlu.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.domain.Place;

public class FloorListAdapter extends BaseAdapter {
    @NonNull
    private Floor[] mList;
    private final LayoutInflater inflater;
    private int selectedPosition = -1;
    private Integer buildingId;

    public FloorListAdapter(@NonNull Context context, @NonNull Place building) {
        this.mList = building.floorList;
        this.inflater = LayoutInflater.from(context.getApplicationContext());
        this.buildingId = building.getId();
    }

    @Override
    public int getCount() {
        return mList.length;
    }

    @Override
    public Floor getItem(int position) {
        return (position >= 0 && position < mList.length) ? mList[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return (position >= 0 && position < mList.length) ? position : -1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.floor_dropdown_item, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.ctv = convertView.findViewById(R.id.ctv_floor_list_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Floor floor = getItem(position);

        if (floor != null) {
            viewHolder.ctv.setText(floor.getName());
        }
        viewHolder.ctv.setChecked(position == selectedPosition);

        return convertView;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setSelectedPosition(int selectedPosition) {
        int pos = -1;
        if (selectedPosition >= 0 && selectedPosition < mList.length) {
            pos = selectedPosition;
        }
        this.selectedPosition = pos;
    }

    public void setSelectedPosition(@NonNull Floor floor) {
        int pos = -1;
        for (int i = 0; i < mList.length; i++) {
            Floor f = mList[i];
            if (f != null && floor.getId().equals(f.getId())) {
                pos = i;
                break;
            }
        }
        selectedPosition = pos;
    }

    public void updateList(@NonNull Place building) {
        if (building.getId().equals(buildingId)) return;
        buildingId = building.getId();
        mList = building.floorList;
        notifyDataSetChanged();
    }

    class ViewHolder {
        CheckedTextView ctv;
    }
}
