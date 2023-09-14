package com.cxb.apicommon.other;

import com.cxb.apicommon.common.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * api接口返回类
 *
 * @author cxb
 */
@Data
public class ApiResult implements Serializable {

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
