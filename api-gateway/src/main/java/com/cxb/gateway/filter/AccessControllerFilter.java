package com.cxb.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * 访问控制过滤器
 *
 * @author cxb
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "bing-api.gateway-filter.access-controller")
public class AccessControllerFilter implements GatewayFilter, Ordered {

    private static final String REQUEST_SOURCE = "bing-api-sdk";
    private boolean enabled;
    private List<String> blackList;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        //请求头
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String requestSource = headers.getFirst("requestSource");
        //黑名单ip和没有请求头的不放行
        String host = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        if (CharSequenceUtil.isBlank(requestSource)
                || !REQUEST_SOURCE.equals(requestSource)
                || blackList.contains(host)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        return chain.filter(exchange);
    }


    @Override
    public int getOrder() {
        return -10;
    }
}
