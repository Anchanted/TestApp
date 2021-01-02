package cn.edu.xjtlu.testapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.edu.xjtlu.testapp.domain.Floor;

public class FloorArrayAdapter extends ArrayAdapter<Floor> {
    public FloorArrayAdapter(@NonNull Context context, int resource, @NonNull Floor[] objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
