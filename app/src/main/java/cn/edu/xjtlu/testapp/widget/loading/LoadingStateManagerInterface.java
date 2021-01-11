package cn.edu.xjtlu.testapp.widget.loading;

public interface LoadingStateManagerInterface {
    void showLoading();

    void showNetworkError();

    void showError();

    void showEmpty();

    void dismiss();

    interface OnClickListener {
        void onClick();
    }
}
