package cn.edu.xjtlu.testapp.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.edu.xjtlu.testapp.MainActivity;
import cn.edu.xjtlu.testapp.R;
import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.domain.Place;

public class FloorAlertDialog {
    private Context mContext;
    private final AlertDialog dialog;
    private final ArrayAdapter<String> adapter;
    private Place building;
    private final List<String> nameList = new ArrayList<>();
    private TextView tvTitle;
    private OnItemClickListener mListener;

    private FloorAlertDialog() {
        dialog = null;
        adapter = null;
    }

    public FloorAlertDialog(Context context, OnItemClickListener listener) {
        mContext = context;
        mListener = listener;
        ViewGroup titleLayout = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.floor_dialog_title, null);
        tvTitle = titleLayout.findViewById(R.id.floor_dialog_title_text);
        adapter = new ArrayAdapter<>(context, R.layout.floor_dialog_item, R.id.floor_dialog_item_text, nameList);
        dialog = new AlertDialog.Builder(mContext)
                .setCustomTitle(titleLayout)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClick(building, which);
                    }
                })
                .setNegativeButton(mContext.getString(R.string.button_cancel), null)
                .create();
        dialog.show();
//        Button button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        button.setAllCaps(false);
//        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
//        button.setTextColor(mContext.getResources().getColor(R.color.bs_primary));
//        dialog.dismiss();
    }

    public void updateData(Place place) {
        if (building != null && building.getId().equals(place.getId())) return;
        building = place;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> extraInfo = (Map<String, Object>) place.getExtraInfo();
        List<Floor> floorList = null;
        try {
            floorList = Arrays.asList(mapper.readValue(mapper.writeValueAsString(extraInfo.get("floorList")), Floor[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        tvTitle.setText(mContext.getString(R.string.choose_floor, place.getCode()));
        nameList.clear();
        for (Floor floor : floorList) {
            nameList.add(floor.getName());
        }
        adapter.notifyDataSetChanged();
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public interface OnItemClickListener {
        void onClick(Place building, int index);
    }
}
