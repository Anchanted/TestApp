package cn.edu.xjtlu.testapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class NetworkUtil {
    private final WifiManager wifiManager;
    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            ToastUtil.shortToastSuccess("onAvailable");
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            ToastUtil.shortToastSuccess("onLost");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            ToastUtil.shortToastSuccess("onUnavailable");
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            String str = "";
            // networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) 是否连通
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    // 使用WI-FI
                    str = "WIFI";
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        int ipInt = wifiInfo.getIpAddress();
                        StringBuilder sb = new StringBuilder();
                        sb.append(ipInt & 0xFF).append(".");
                        sb.append((ipInt >> 8) & 0xFF).append(".");
                        sb.append((ipInt >> 16) & 0xFF).append(".");
                        sb.append((ipInt >> 24) & 0xFF);
                        LogUtil.d("NetworkUtil", wifiInfo.getSSID() + " " + sb.toString());
                    }
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ) {
                    // 使用蜂窝网络
                    str = "CELLULAR";
                } else{
                    // 未知网络，包括蓝牙、VPN、LoWPAN
                    str = "UNKNOWN";
                }
            }
            ToastUtil.shortToastSuccess("onCapabilitiesChanged " + str);
            LogUtil.d("NetworkUtil", "" + networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
    };

    public NetworkUtil(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            NetworkRequest request = builder.build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    public void unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
