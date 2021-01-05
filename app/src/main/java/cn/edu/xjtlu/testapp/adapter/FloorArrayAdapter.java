package cn.edu.xjtlu.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.domain.Floor;

public class FloorArrayAdapter extends ArrayAdapter<Floor> {
    private int selectedPosition;

    public FloorArrayAdapter(@NonNull Context context, @NonNull List<Floor> objects, int selectedPosition) {
        super(context, 0, objects);
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent,false);
        }

        TextView tv = convertView.findViewById(R.id.tv_floor_name);
        Floor floor = getItem(position);

        if (floor != null) {
            tv.setText(floor.getName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown_item, parent,false);
        }

        CheckedTextView ctv = convertView.findViewById(R.id.ctv_floor_list_name);
        Floor floor = getItem(position);

        if (floor != null) {
            ctv.setText(floor.getName());
        }
        if (position == selectedPosition) {
            ctv.setChecked(true);
        }

        return convertView;
    }
}
