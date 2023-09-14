package com.cxb.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.cxb.apiclientsdk.utils.SignUtil;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.InterfaceInfo;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.service.ApiBackendService;
import com.cxb.apicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 身份校验过滤器
 *
 * @author cxb
 */
@Component
@Slf4j
public class AuthenticationFilter implements GatewayFilter, Ordered {

    @DubboReference
    private ApiBackendService apiBackendService;

    @DubboReference
    private InnerUserService innerUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String url = headers.getFirst("url");
        String accessKey = headers.getFirst("accessKey");
        String once = headers.getFirst("once");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String method = Objects.requireNonNull(request.getMethod()).toString();
        // 判断是否有空
        if (CharSequenceUtil.hasBlank(url, accessKey, once, timestamp, sign)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        //查询分配给该用户的密钥校验accessKey
        User invokeUser = innerUserService.getInvokeUser(accessKey);
        if (invokeUser == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "拒绝访问,未分配密钥");
        }
        //判断时间不能超过2分钟（超时直接删除redis随机数，解决服务器和前端的时间差问题,减轻redis压力）
        long now = System.currentTimeMillis() / 1000;
        long passTime = Long.parseLong(timestamp) + 60 * 2;
        if (now > passTime) {
            stringRedisTemplate.delete("api:uuid:" + once);
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "拒绝访问,请求过期");
        }
        //校验随机值,防重放(存入redis，存在就拦截)
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent("api:uuid:" + once, "", 2, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(aBoolean)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "拒绝访问,请求重放");
        }
        //校验签名
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtil.getSign(once, timestamp, secretKey);
        if (!serverSign.equals(sign)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "拒绝访问,签名认证失败");
        }
        //请求的模拟接口是否存在
        InterfaceInfo invokeInterfaceInfo = apiBackendService.getInvokeInterfaceInfo(url, method);
        if (invokeInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口信息不存在");
        }
        //校验剩余调用次数以及接口次数统计
        if (!invokeUser.getUserRole().equals("admin")) {
            Boolean[] b = apiBackendService.invokeInterfaceCount(invokeUser.getId(), invokeInterfaceInfo.getId());
            if (Boolean.FALSE.equals(b[0])) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"剩余接口调用次数不足");
            }
            if (Boolean.FALSE.equals(b[1])) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口次数统计失败");
            }
        }
        //传递参数到响应过滤器
        ServerHttpRequest newRequest = request.mutate()
                .header("interface_id", invokeInterfaceInfo.getId().toString())
                .header("user_id",invokeUser.getId().toString())
                .build();
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    @Override
    public int getOrder() {
        return -9;
    }
}
