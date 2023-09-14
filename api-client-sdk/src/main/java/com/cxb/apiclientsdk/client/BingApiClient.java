package com.cxb.apiclientsdk.client;

import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apiclientsdk.common.HttpMethod;
import com.cxb.apiclientsdk.factory.ApiClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cxb
 */
public class BingApiClient {

    private final Map<String, ApiClient> clientMap;


    /**
     * 客户端初始化,并本地缓存到map
     *
     * @param accessKey accessKey
     * @param secretKey secretKey
     */
    public BingApiClient(String accessKey, String secretKey) {
        //初始化客户端
        this.clientMap = new HashMap<>();
        ApiClientFactory apiClientFactory = new ApiClientFactory(accessKey, secretKey);
        this.clientMap.put(HttpMethod.GET.getValue(), apiClientFactory.createClient(HttpMethod.GET));
        this.clientMap.put(HttpMethod.POST.getValue(), apiClientFactory.createClient(HttpMethod.POST));
        this.clientMap.put(HttpMethod.PUT.getValue(), apiClientFactory.createClient(HttpMethod.PUT));
        this.clientMap.put(HttpMethod.DELETE.getValue(), apiClientFactory.createClient(HttpMethod.DELETE));
    }

    /**
     * 调用方法
     *
     * @param url      url
     * @param method   请求方法
     * @param paramMap 请求参数
     * @return ApiResult
     */
    public ApiResult invoke(String url, String method, Map<String, Object> paramMap) {
        method = method.toUpperCase();
        return clientMap.get(method).invoke(url, method, paramMap);
    }


}
