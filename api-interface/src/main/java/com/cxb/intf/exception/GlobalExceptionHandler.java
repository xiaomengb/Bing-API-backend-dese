package com.cxb.intf.exception;

import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.other.ApiResult;
import com.cxb.apicommon.other.ApiResultUtils;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author cxb
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult businessExceptionHandler(BusinessException e) {
        log.error("businessException: " + e.getMessage(), e);
        return ApiResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResult runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ApiResultUtils.error(ErrorCode.SYSTEM_ERROR,"接口系统异常");
    }
}
