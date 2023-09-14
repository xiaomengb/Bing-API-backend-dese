package com.cxb.apiclientsdk.client;

import com.cxb.apiclientsdk.common.ApiResult;

import java.util.Map;

/**
 * 接口调用方法定义
 *
 * @author cxb
 */
public interface ApiClient {

    /**
     *  接口调用方法
     * @param url 请求地址
     * @param method 请求方法
     * @param paramsMap 参数集合
     * @return 返回包装类
     */
    ApiResult invoke(String url, String method, Map<String,Object> paramsMap);
}
