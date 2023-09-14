package com.cxb.gateway.handler;

import cn.hutool.json.JSONUtil;

import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apiclientsdk.common.ResultUtils;
import com.cxb.apicommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关异常通用处理器，只作用在webflux 环境下 , 优先级低于 {@link ResponseStatusExceptionHandler} 执行
 * author: cxb
 */
@Slf4j
@Order(-1)
@Configuration
public class GlobalExceptionConfiguration implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应状态码为 500
        //response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 构建自定义结果类
        ApiResult apiResult = null;
        if (ex instanceof BusinessException) {
            log.error("businessException: " + ex.getMessage(), ex);
            apiResult = ResultUtils.error(((BusinessException) ex).getCode(), ex.getMessage());
        }
        if (ex instanceof ResponseStatusException) {
            log.error("responseStatusException: " + ex.getMessage(), ex);
            apiResult = ResultUtils.error(((ResponseStatusException) ex).getStatus().value(), "响应状态异常");
        }
        // 将结果类转换为 JSON 字符串
        String responseBody = JSONUtil.toJsonStr(apiResult);
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        // 返回响应
        return response.writeWith(Mono.just(buffer));
    }

}