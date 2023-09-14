package com.cxb.apiclientsdk.client;


import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apiclientsdk.common.ErrorCode;
import com.cxb.apiclientsdk.common.ResultUtils;
import com.cxb.apiclientsdk.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用第三方接口的客户端
 *
 * @author cxb
 */
@Slf4j
public abstract class DefaultAbstractApiClient implements ApiClient {

    private String accessKey;

    private String secretKey;

    protected DefaultAbstractApiClient() {
    }

    protected DefaultAbstractApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }


    public Map<String, String> getHeaderMap() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("requestSource", "bing-api-sdk");
        headers.put("accessKey", accessKey);
        String once = IdUtil.fastSimpleUUID();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        headers.put("once", once);
        headers.put("timestamp", timestamp);
        headers.put("sign", SignUtil.getSign(once, timestamp, secretKey));
        return headers;
    }

    public ApiResult getApiResult(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            if (httpResponse.getStatus() == HttpStatus.HTTP_BAD_REQUEST) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "http请求失败");
            }
            if (httpResponse.getStatus() == HttpStatus.HTTP_BAD_REQUEST) {
                return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "http请求未找到");
            }
        }
        String result = httpResponse.body();
        return JSONUtil.toBean(result, ApiResult.class);
    }


}
