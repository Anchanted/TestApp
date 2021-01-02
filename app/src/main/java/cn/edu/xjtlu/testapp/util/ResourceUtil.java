package cn.edu.xjtlu.testapp.util;

import cn.edu.xjtlu.testapp.BuildConfig;

public class ResourceUtil {
    public static String resourceUri(String uri) {
        return String.format(BuildConfig.RESOURCE_ENDPOINT, uri);
    }
}
