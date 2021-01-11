package cn.edu.xjtlu.testapp.widget.loading;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import cn.edu.xjtlu.testapp.R;

// https://github.com/wpq2014/android-blog-samples
public class LoadingStateManager implements LoadingStateManagerInterface {
    private final Context context;
    private final View targetView;
    private final LayoutInflater layoutInflater;
    private ViewGroup parentView;
    private int targetViewInsertIndex;
    private ViewGroup.LayoutParams layoutParams;

    // loading
    private View loadingView;
    private final boolean displayLoadingMessage;
    private final String loadingMessage;
    // network error
    private View networkErrorView;
    private final boolean displayNetworkErrorImage;
    private final int networkErrorImageResource;
    private final Drawable networkErrorImageDrawable;
    private final String networkErrorMessage;
    // other error
    private View errorView;
    private final String errorMessage;
    // empty
    private View emptyView;
    private final boolean displayEmptyImage;
    private final int emptyImageResource;
    private final Drawable emptyImageDrawable;
    private final String emptyMessage;

    // listener
    private final String networkErrorRetryText;
    private final LoadingStateManagerInterface.OnClickListener onNetworkErrorRetryClickListener;
    private final String errorRetryText;
    private final LoadingStateManagerInterface.OnClickListener onErrorRetryClickListener;

    private LoadingStateManager(Builder builder) {
        context = builder.context;
        targetView = builder.targetView;

        loadingView = builder.customLoadingView;
        displayLoadingMessage = builder.displayLoadingMessage;
        loadingMessage = builder.loadingMessage;

        networkErrorView = builder.customNetworkErrorView;
        displayNetworkErrorImage = builder.displayNetworkErrorImage;
        networkErrorImageResource = builder.networkErrorImageResource;
        networkErrorImageDrawable = builder.networkErrorImageDrawable;
        networkErrorMessage = builder.networkErrorMessage;

        errorView = builder.customErrorView;
        errorMessage = builder.errorMessage;

        emptyView = builder.customEmptyView;
        displayEmptyImage = builder.displayEmptyImage;
        emptyImageResource = builder.emptyImageResource;
        emptyImageDrawable = builder.emptyImageDrawable;
        emptyMessage = builder.emptyMessage;

        networkErrorRetryText = builder.networkErrorRetryText;
        onNetworkErrorRetryClickListener = builder.onNetworkErrorRetryClickListener;
        errorRetryText = builder.errorRetryText;
        onErrorRetryClickListener = builder.onErrorRetryClickListener;

        layoutInflater = LayoutInflater.from(context);
        init();
    }

    private void init() {
        layoutParams = targetView.getLayoutParams();
        if (targetView.getParent() != null) {
            parentView = (ViewGroup) targetView.getParent();
        } else {
            parentView = (ViewGroup) targetView.getRootView().findViewById(android.R.id.content);
        }
        targetViewInsertIndex = parentView.indexOfChild(targetView) + 1;
//        int count = parentView.getChildCount();
//        for (int i = 0; i < count; i++) {
//            if (targetView == parentView.getChildAt(i)) {
//                targetViewIndex = i;
//                break;
//            }
//        }
    }

    /**
     * 切换状态
     * @param view 目标View
     */
    private void showView(View view) {
        // 如果当前状态和要切换的状态相同，则不做处理，反之切换
        if (parentView.getChildAt(targetViewInsertIndex) == view) return;
        // 先把view从父布局移除
        ViewGroup viewParent = (ViewGroup) view.getParent();
        if (viewParent != null) {
            viewParent.removeView(view);
        }
//        if (targetViewInsertIndex < parentView.getChildCount()) {
//            parentView.removeViewAt(targetViewInsertIndex);
//        }
        dismiss();
        parentView.addView(view, targetViewInsertIndex, layoutParams);
    }

    @SuppressLint("InflateParams")
    @Override
    public void showLoading() {
        if (loadingView != null) {
            showView(loadingView);
            return;
        }

        loadingView = layoutInflater.inflate(R.layout.loading, null);
        TextView tv = (TextView) loadingView.findViewById(R.id.loading_tv);

        if (!displayLoadingMessage) {
            tv.setVisibility(View.GONE);
        } if (!StringUtils.isEmpty(loadingMessage)) {
            tv.setText(loadingMessage);
        }

        showView(loadingView);
    }

    @SuppressLint("InflateParams")
    @Override
    public void showNetworkError() {
        if (networkErrorView != null) {
            showView(networkErrorView);
            return;
        }

        networkErrorView = layoutInflater.inflate(R.layout.loading_network_error, null);
        ImageView iv = (ImageView) networkErrorView.findViewById(R.id.loading_network_error_iv);
        TextView tv = (TextView) networkErrorView.findViewById(R.id.loading_network_error_tv);
        Button button = (Button) networkErrorView.findViewById(R.id.loading_network_error_button);

        if (!displayNetworkErrorImage) {
            iv.setVisibility(View.GONE);
        } else if (networkErrorImageResource != 0){
            iv.setImageResource(networkErrorImageResource);
        } else if (networkErrorImageDrawable != null) {
            iv.setImageDrawable(networkErrorImageDrawable);
        }

        if (!StringUtils.isEmpty(networkErrorMessage)) {
            tv.setText(networkErrorMessage);
        }

        if (!StringUtils.isEmpty(networkErrorRetryText)) {
            button.setText(networkErrorRetryText);
        }
        if (onNetworkErrorRetryClickListener != null) {
            button.setOnClickListener(v -> onNetworkErrorRetryClickListener.onClick());
        }

        showView(networkErrorView);
    }

    @SuppressLint("InflateParams")
    @Override
    public void showError() {
        if (errorView != null) {
            showView(errorView);
            return;
        }

        errorView = layoutInflater.inflate(R.layout.loading_error, null);
        TextView tv = (TextView) errorView.findViewById(R.id.loading_error_tv);
        Button button = (Button) errorView.findViewById(R.id.loading_error_button);

        if (!StringUtils.isEmpty(errorMessage)) {
            tv.setText(errorMessage);
        }

        if (!StringUtils.isEmpty(errorRetryText)) {
            button.setText(errorRetryText);
        }
        if (onErrorRetryClickListener != null) {
            button.setOnClickListener(v -> onErrorRetryClickListener.onClick());
        }

        showView(errorView);
    }

    @SuppressLint("InflateParams")
    @Override
    public void showEmpty() {
        if (emptyView != null) {
            showView(emptyView);
            return;
        }

        emptyView = layoutInflater.inflate(R.layout.loading_empty, null);
        ImageView iv = (ImageView) emptyView.findViewById(R.id.loading_empty_iv);
        TextView tv = (TextView) emptyView.findViewById(R.id.loading_empty_tv);

        if (!displayEmptyImage) {
            iv.setVisibility(View.GONE);
        } else if (emptyImageResource != 0){
            iv.setImageResource(emptyImageResource);
        } else if (emptyImageDrawable != null) {
            iv.setImageDrawable(emptyImageDrawable);
        }

        if (!StringUtils.isEmpty(emptyMessage)) {
            tv.setText(emptyMessage);
        }

        showView(emptyView);
    }

    @Override
    public void dismiss() {
        if (loadingView != null && parentView.indexOfChild(loadingView) > -1) {
            parentView.removeView(loadingView);
        }
        if (networkErrorView != null && parentView.indexOfChild(networkErrorView) > -1) {
            parentView.removeView(networkErrorView);
        }
        if (errorView != null && parentView.indexOfChild(errorView) > -1) {
            parentView.removeView(errorView);
        }
        if (emptyView != null && parentView.indexOfChild(emptyView) > -1) {
            parentView.removeView(emptyView);
        }
    }

    public static class Builder{
        private final Context context;
        private final View targetView;
        // loading
        private View customLoadingView;
        private boolean displayLoadingMessage;
        private String loadingMessage;
        // network error
        private View customNetworkErrorView;
        private boolean displayNetworkErrorImage;
        private int networkErrorImageResource;
        private Drawable networkErrorImageDrawable;
        private String networkErrorMessage;
        // error
        private View customErrorView;
        private String errorMessage;
        // empty
        private View customEmptyView;
        private boolean displayEmptyImage;
        private int emptyImageResource;
        private Drawable emptyImageDrawable;
        private String emptyMessage;

        // listener
        private String networkErrorRetryText;
        private LoadingStateManagerInterface.OnClickListener onNetworkErrorRetryClickListener;
        private String errorRetryText;
        private LoadingStateManagerInterface.OnClickListener onErrorRetryClickListener;

        public Builder(@NonNull Context context, @NonNull View targetView) {
            this.context = context;
            this.targetView = targetView;
        }

        public Builder setLoadingView(View loadingView) {
            this.customLoadingView = loadingView;
            this.displayLoadingMessage = false;
            return this;
        }

        public Builder setLoadingView(View loadingView, String loadingMessage) {
            this.customLoadingView = loadingView;
            this.loadingMessage = loadingMessage;
            this.displayLoadingMessage = true;
            return this;
        }

        public Builder setNetworkErrorView(View networkErrorView, String networkErrorMessage) {
            this.customNetworkErrorView = networkErrorView;
            this.displayNetworkErrorImage = false;
            this.networkErrorMessage = networkErrorMessage;
            return this;
        }

        public Builder setNetworkErrorView(View networkErrorView, int networkErrorImageResource, String networkErrorMessage) {
            this.customNetworkErrorView = networkErrorView;
            this.networkErrorImageResource = networkErrorImageResource;
            this.displayNetworkErrorImage = true;
            this.networkErrorMessage = networkErrorMessage;
            return this;
        }

        public Builder setNetworkErrorView(View networkErrorView, Drawable networkErrorImageDrawable, String networkErrorMessage) {
            this.customNetworkErrorView = networkErrorView;
            this.networkErrorImageDrawable = networkErrorImageDrawable;
            this.displayNetworkErrorImage = true;
            this.networkErrorMessage = networkErrorMessage;
            return this;
        }

        public Builder setErrorView(View errorView, String errorMessage) {
            this.customErrorView = errorView;
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder setEmptyView(View empty, String emptyMessage) {
            this.customEmptyView = empty;
            this.displayNetworkErrorImage = false;
            this.emptyMessage = emptyMessage;
            return this;
        }

        public Builder setEmptyView(View empty, int emptyImageResource, String emptyMessage) {
            this.customEmptyView = empty;
            this.emptyImageResource = emptyImageResource;
            this.displayEmptyImage = true;
            this.emptyMessage = emptyMessage;
            return this;
        }

        public Builder setEmptyView(View empty, Drawable emptyImageDrawable, String emptyMessage) {
            this.customEmptyView = empty;
            this.emptyImageDrawable = emptyImageDrawable;
            this.displayEmptyImage = true;
            this.emptyMessage = emptyMessage;
            return this;
        }

        public Builder setOnNetworkErrorRetryClickListener(String networkErrorRetryText, LoadingStateManagerInterface.OnClickListener listener) {
            this.networkErrorRetryText = networkErrorRetryText;
            this.onNetworkErrorRetryClickListener = listener;
            return this;
        }

        public Builder setOnErrorRetryClickListener(String errorRetryText, LoadingStateManagerInterface.OnClickListener listener) {
            this.errorRetryText = errorRetryText;
            this.onErrorRetryClickListener = listener;
            return this;
        }

        public LoadingStateManager build() {
            return new LoadingStateManager(this);
        }
    }
}
