package com.cxb.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.common.JwtUtils;
import com.cxb.apicommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoginGlobalFilter implements GlobalFilter, Ordered {


    protected static final List<String> NOT_LOGIN_PATH = Arrays.asList(
            "/api/backend/user/login", "/api/backend/user/loginByPhone",
            "/api/backend/user/register", "/api/backend/user/captcha",
            "/api/backend/user/smsCaptcha", "/api/interface/**", "/api/third/alipay/**");


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = request.getHeaders();


        //登录过滤
        String path = request.getPath().toString();
        //判断请求路径是否需要登录
        List<Boolean> collect = NOT_LOGIN_PATH.stream().map(notLoginPath -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            return antPathMatcher.match(notLoginPath, path);
        }).collect(Collectors.toList());

        if (collect.contains(true)) {
            return chain.filter(exchange);
        }

        //校验cookie 中的token
        MultiValueMap<String, HttpCookie> cookiesMap = request.getCookies();
        Collection<List<HttpCookie>> cookiesList = cookiesMap.values();
        if (cookiesList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (Boolean.FALSE.equals(getLoginUserByCookie(cookiesList))) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return chain.filter(exchange);
    }

    private Boolean getLoginUserByCookie(Collection<List<HttpCookie>>  cookiesList) {
        for (List<HttpCookie> httpCookies : cookiesList) {
            for (HttpCookie cookie : httpCookies) {
                if(cookie.getName().equals("token")){
                    return JwtUtils.checkToken(cookie.getValue());
                }
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}