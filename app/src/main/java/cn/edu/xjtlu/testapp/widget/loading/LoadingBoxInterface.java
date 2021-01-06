package cn.edu.xjtlu.testapp.widget.loading;

public interface LoadingBoxInterface {
    void showLoading();

    void showNetworkError();

    void showError();

    void showEmpty();

    void dismissLoading();

    interface OnClickListener {
        void onClick();
    }
}
