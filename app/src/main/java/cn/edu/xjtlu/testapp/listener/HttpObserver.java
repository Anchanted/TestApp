package cn.edu.xjtlu.testapp.listener;

import android.app.Activity;

import cn.edu.xjtlu.testapp.domain.response.Result;
import cn.edu.xjtlu.testapp.util.HttpUtil;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class HttpObserver<T> extends ObserverAdapter<T> {
    private static final String TAG = HttpObserver.class.getSimpleName();
    private Activity activity;
//    private boolean showLoading;

    public HttpObserver() {}

    public HttpObserver(Activity activity, boolean showLoading) {
        this.activity = activity;
//        this.showLoading = showLoading;
    }

    public void onSucceed(T data) {
    }

    public boolean onFailed(T data, Throwable e) {
        return false;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        super.onSubscribe(d);
//        if (this.showLoading) {
//            LoadingUtil.showLoading(activity);
//        }
    }

    @Override
    public void onNext(@NonNull T t) {
        super.onNext(t);

//        checkHideLoading();

        if (isSucceed(t)) {
            onSucceed(t);
        } else{
            requestErrorHandler(t, null);
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        super.onError(e);

//        checkHideLoading();

        requestErrorHandler(null, e);
    }

//    private void checkHideLoading() {
//        if (this.showLoading) {
//            LoadingUtil.hideLoading();
//        }
//    }

    protected boolean isSucceed(T t) {
        if (t instanceof Result) {
            Result result = (Result) t;
            return result.getCode() == 1;
        }

        return false;
    }

    protected void requestErrorHandler(T data, Throwable e) {
        if (onFailed(data, e)) return;

        HttpUtil.requestErrorHandler(data, e);

        onFailed(null, e);
    }
}
