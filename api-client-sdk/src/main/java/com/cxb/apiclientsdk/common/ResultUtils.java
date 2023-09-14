package com.cxb.apiclientsdk.common;

import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apiclientsdk.common.ErrorCode;

/**
 * 返回工具类
 *
 * @author cxb
 */
public class ResultUtils {

    private ResultUtils() {
    }

    /**
     * 成功
     *
     * @param data
     * @return
     */
    public static ApiResult success(Object data) {
        return new ApiResult(0, data, "ok");
    }

    /**
     * 成功
     *
     * @return
     */
    public static  ApiResult success() {
        return new ApiResult(0, null, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static ApiResult error(ErrorCode errorCode) {
        return new ApiResult(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static ApiResult error(int code, String message) {
        return new ApiResult(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static ApiResult error(ErrorCode errorCode, String message) {
        return new ApiResult(errorCode.getCode(), null, message);
    }
}
