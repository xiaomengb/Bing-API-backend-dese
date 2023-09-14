package com.cxb.intf.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author cxb
 */
public class RedirectUtil {
    private RedirectUtil() {
    }

    /**
     * 获取重定向地址
     *
     * @param url url
     * @return
     */
    public static String getRedirectUrl(String url) throws IOException {

        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        // 设置连接跟随重定向
        connection.setInstanceFollowRedirects(true);

        // 获取HTTP响应代码
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 如果响应代码是200 OK，则获取最终的URL
            return connection.getURL().toString();
        }

        // 关闭连接
        connection.disconnect();
        return null;

    }
}
