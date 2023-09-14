package com.cxb.apiclientsdk.common;

/**
 * http请求方法枚举值
 */
public enum HttpMethod {
    /**
     * 请求类型
     */
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    /**
     * 请求类型值
     */
    private String value;

    public String getValue() {
        return value;
    }

    HttpMethod(String value) {
        this.value = value;
    }
}
