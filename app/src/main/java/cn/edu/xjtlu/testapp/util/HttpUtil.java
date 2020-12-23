package cn.edu.xjtlu.testapp.util;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.edu.xjtlu.testapp.api.Result;
import retrofit2.HttpException;

public class HttpUtil {
    public static void requestErrorHandler(Object data, Throwable e) {
        String message = null;
        if (e != null) {
            if (e instanceof UnknownHostException) {
                message = "找不到服务器，请稍后再试！";
            } else if (e instanceof ConnectException) {
                message = "连接失败，请稍后再试！";
            } else if (e instanceof SocketTimeoutException) {
                message = "连接超时，请稍后再试！";
            } else if (e instanceof HttpException) {
                HttpException exception = (HttpException) e;
                int code = exception.code();
                if (code == 401) {
                    message = "登录信息过期，请重新登录！";
                } else if (code == 403) {
                    message = "你没有权限访问！";
                } else if (code == 404) {
                    message = "你访问的内容不存在！";
                } else if (code >= 500) {
                    message = "服务器错误，请稍后再试！";
                } else {
                    message = "未知错误，请稍后再试！";
                }
            } else {
                message = "未知错误，请稍后再试！";
            }
        } else {
            if (data instanceof Result && data != null) {
                Result result = (Result) data;
                String resultMsg = result.getMsg();
                message = resultMsg == null || resultMsg.isEmpty() ? "未知错误，请稍后再试！" : resultMsg;
            }
        }
    }
}
