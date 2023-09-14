package com.cxb.apiclientsdk.utils;

import java.util.Map;

/**
 * restful风格工具类
 *
 * @author cxb
 */
public class RestfulUtil {

    private RestfulUtil() {
    }

    /**
     * 判断是否为restful风格url
     *
     * @param url url
     * @return boolean
     */
    public static boolean isRestfulPath(String url) {
        //根据斜号分割
        String[] splits = url.split("/");
        //从后往前校验
        for (int i = splits.length - 1; i >= 0; i--) {
            String split = splits[i];
            if (split.isEmpty()) {
                //为空则跳过
                continue;
            }
            //判断该段是否为动态参数 {param}
            if (split.startsWith("{") && split.endsWith("}")) {
                return true;
            }
        }
        return false;
    }

    /**
     * restful风格请求替换参数,普通的拼接参数
     *
     * @param url url
     * @param map 请求参数
     * @return url
     */
    public static String buildUrl(String url, Map<String, Object> map) {
        if (isRestfulPath(url)) {
            //满足restful
            StringBuilder sb = new StringBuilder();
            String[] splits = url.split("/");
            for (String split : splits) {
                if (split.isEmpty()) {
                    continue;
                }
                Object value = split;
                //替换参数
                if (split.startsWith("{") && split.endsWith("}")) {
                    String key = split.substring(1, split.length() - 1);
                    value = map.get(key);
                    if (value == null) {
                        throw new IllegalArgumentException("参数: " + key + "没找到");
                    }
                }
                sb.append("/").append(value);
            }
            //去掉最前面的 /
            sb.deleteCharAt(0);
            //补上http://
            sb.insert(sb.indexOf(":")+1,"/");
            return sb.toString();
        } else {
            //不满足restful
            StringBuilder sb = new StringBuilder(url);
            if (!map.isEmpty()) {
                //拼接参数
                sb.append("?");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                //去除最后的 &
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    }
}
