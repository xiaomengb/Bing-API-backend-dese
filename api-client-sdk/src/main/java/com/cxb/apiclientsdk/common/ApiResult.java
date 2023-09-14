package com.cxb.apiclientsdk.common;

import lombok.Data;

/**
 * api接口返回类
 *
 * @author cxb
 */
@Data
public class ApiResult {

    private int code;

    private Object data;

    private String message;

    public ApiResult(int code, Object data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public ApiResult(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public ApiResult(int code, Object data) {
        this(code, data, "");
    }

}
