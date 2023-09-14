package com.cxb.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.cxb.apiclientsdk.common.ApiResult;

import java.util.Map;

/**
 * PUT 请求客户端
 * @author cxb
 */
public class PutApiClient extends DefaultAbstractApiClient{


    public PutApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    @Override
    public ApiResult invoke(String url, String method, Map<String, Object> paramsMap) {

        HttpResponse httpResponse = HttpRequest.put(url)
                .header("url", url)
                .addHeaders(getHeaderMap())
                //.contentType("application/json;charset=UTF-8")
                .form(paramsMap)
                .timeout(30000)
                .execute();
        return getApiResult(httpResponse);
    }
}
