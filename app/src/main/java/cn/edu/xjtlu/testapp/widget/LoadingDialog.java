package cn.edu.xjtlu.testapp.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.edu.xjtlu.testapp.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        this(context, null);
    }

    public LoadingDialog(Context context, View.OnClickListener listener) {
        super(context, R.style.LoadingDialog);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        Button closeButton = view.findViewById(R.id.close_button);
        if (listener != null) {
            closeButton.setOnClickListener(listener);
        } else {
            closeButton.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(view, params);
    }
}
