package com.cxb.gateway.filter;

import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.dto.message.UserInterfaceMessage;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cxb.apicommon.constant.RabbitmqConstant.API_INTERFACE_EXCHANGE;
import static com.cxb.apicommon.constant.RabbitmqConstant.API_INTERFACE_ROLLBACK_KEY;

/**
 * 响应过滤器
 *
 * @author cxb
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "bing-api.gateway-filter.log.response.enabled", havingValue = "true", matchIfMissing = true)
public class ResponseLogFilter implements GatewayFilter, Ordered {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            //从交换寄拿响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //拿到从身份校验过滤器传递来的参数
            ServerHttpRequest request = exchange.getRequest();
            Long interfaceId = Long.valueOf(Objects.requireNonNull(request.getHeaders().getFirst("interface_id")));
            Long userId = Long.valueOf(Objects.requireNonNull(request.getHeaders().getFirst("user_id")));
            //缓冲区工厂，拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                //自定义响应装饰器，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //对象是响应式的
                        if (body instanceof Flux) {
                            //我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //往返回值里面写数据
                            //拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存

                                // 调用失败，利用消息队列回滚接口统计
                                if (this.getStatusCode() != HttpStatus.OK) {
                                    UserInterfaceMessage userInterfaceMessage = new UserInterfaceMessage(userId, interfaceId);
                                    rabbitTemplate.convertAndSend(API_INTERFACE_EXCHANGE, API_INTERFACE_ROLLBACK_KEY, userInterfaceMessage);
                                }
                                //处理400-500 页面找不到
                                if (Objects.requireNonNull(this.getStatusCode()).value() >= 400 && this.getStatusCode().value() < 500) {
                                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "请求地址不存在");
                                }
                                //打印响应日志
                                printResponseLog(content, request, originalResponse);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 调用失败，打印错误码
                            log.error("--------< 错误码： {}", this.getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        } catch (BusinessException e) {
            throw new BusinessException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }


    private void printResponseLog(byte[] content, ServerHttpRequest request, ServerHttpResponse response) {
        // 构建日志
        String data = new String(content, StandardCharsets.UTF_8);
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        // 打印请求路径
        String path = request.getPath().pathWithinApplication().value();
        String requestUrl = UriComponentsBuilder.fromPath(path).queryParams(queryParams).build().toUriString();
        // 构建成一条长日志
        StringBuilder responseLog = new StringBuilder(200);
        // 日志参数
        List<Object> responseArgs = new ArrayList<>();
        responseLog.append("\n================ Gateway Response Start  ================\n");
        responseLog.append("<=== id: {}\n");
        responseArgs.add(request.getId());
        //请求类型、UrI get: /xxx/xxx/xxx?a=b
        responseLog.append("<=== method: {}\n");
        String requestMethod = request.getMethodValue();
        responseArgs.add(requestMethod);
        responseLog.append("<=== uri: {}\n");
        responseArgs.add(requestUrl);
        //状态码
        responseLog.append("<=== code: {}\n");
        responseArgs.add(Objects.requireNonNull(response.getStatusCode()).value());
        //响应结果
        responseLog.append("<=== data: {}\n");
        responseArgs.add(data);
        responseLog.append("================  Gateway Response End  =================\n");
        // 打印执行时间
        log.info(responseLog.toString(), responseArgs.toArray());
    }

    @Override
    public int getOrder() {
        return -7;
    }
}
