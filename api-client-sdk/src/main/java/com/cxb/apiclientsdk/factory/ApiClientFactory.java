package com.cxb.apiclientsdk.factory;

import com.cxb.apiclientsdk.client.*;
import com.cxb.apiclientsdk.common.HttpMethod;

/**
 * 客户端创建工厂
 *
 * @author cxb
 */
public class ApiClientFactory {

    private final String accessKey;

    private final String secretKey;

    public ApiClientFactory(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public ApiClient createClient(HttpMethod method) {
        if (method == HttpMethod.GET) {
            return new GetApiClient(accessKey, secretKey);
        } else if (method == HttpMethod.POST) {
            return new PostApiClient(accessKey, secretKey);
        } else if (method == HttpMethod.PUT) {
            return new PutApiClient(accessKey, secretKey);
        } else if (method == HttpMethod.DELETE) {
            return new DeleteApiClient(accessKey, secretKey);
        }
        return null;
    }
}
