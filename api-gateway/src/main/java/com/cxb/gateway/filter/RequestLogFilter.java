package com.cxb.gateway.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求日志过滤器
 *
 * @author cxb
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "bing-api.gateway-filter.log.request.enabled", havingValue = "true", matchIfMissing = true)
public class RequestLogFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 打印请求路径
        String path = request.getPath().pathWithinApplication().value();
        MultiValueMap<String, String> queryParams = request.getQueryParams();

        // 构建成一条长日志，避免并发下日志错乱
        StringBuilder reqLog = new StringBuilder(200);
        // 日志参数
        List<Object> reqArgs = new ArrayList<>();
        reqLog.append("\n================ Gateway Request Start  ================\n");
        // 打印路由添加占位符
        reqLog.append("===> id: {}\n");
        reqArgs.add(request.getId());
        reqLog.append("===> {}: {}\n");
        reqLog.append("===> queryParams: {}\n");
        // 参数
        String requestMethod = request.getMethodValue();
        reqArgs.add(requestMethod);
        reqArgs.add(path);
        reqArgs.add(queryParams);

        // 打印请求头
        HttpHeaders headers = request.getHeaders();
        headers.forEach((headerName, headerValue) -> {
            reqLog.append("===Headers===  {}: {}\n");
            reqArgs.add(headerName);
            reqLog.append("===Headers===  {}: {}\n");
            reqArgs.add(headerName.concat("-original"));
            reqArgs.add(StrUtil.join(",", headerValue.toArray()));
            reqArgs.add(StrUtil.join(",", headerValue.toArray()));
        });

        reqLog.append("================  Gateway Request End  =================\n");
        // 打印执行时间
        log.info(reqLog.toString(), reqArgs.toArray());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -8;
    }
}
