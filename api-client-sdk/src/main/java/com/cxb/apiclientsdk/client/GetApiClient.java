package com.cxb.apiclientsdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apiclientsdk.utils.RestfulUtil;

import java.util.Map;

/**
 * GET 请求客户端
 *
 * @author cxb
 */
public class GetApiClient extends DefaultAbstractApiClient {


    public GetApiClient(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    @Override
    public ApiResult invoke(String url, String method, Map<String, Object> paramsMap) {
        //判断url是否为restful风格
        String originalUrl = url;
        url = RestfulUtil.buildUrl(url, paramsMap);
        HttpResponse httpResponse = HttpRequest.get(url)
                .header("url", originalUrl)
                .addHeaders(getHeaderMap())
                //.contentType("application/json;charset=UTF-8")
                .timeout(30000)
                .execute();
        return getApiResult(httpResponse);
    }


}
