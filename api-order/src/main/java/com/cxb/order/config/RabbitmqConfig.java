package com.cxb.order.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;

/**
 * 死信队列绑定订单超时队列
 *
 * @author cxb
 */
@Configuration
@Slf4j
public class RabbitmqConfig {

    @Bean
    public DirectExchange ORDER_TIMEOUT_EXCHANGE(){
        return new DirectExchange(ORDER_TIMEOUT_EXCHANGE,true,false);
    }

    @Bean
    public Queue ORDER_TIMEOUT_QUEUE(){
        Map<String, Object> args = new HashMap<>(2);
        //正常队列中的消息被废弃后会被路由到死信队列(前提是有绑定死信队列)
        // 绑定我们的死信交换机
        args.put("x-dead-letter-exchange", DEAD_ORDER_TIMEOUT_EXCHANGE);
        // 绑定我们的路由key
        args.put("x-dead-letter-routing-key", DEAD_ORDER_TIMEOUT_KEY);
        args.put("x-message-ttl", 10*60000);
        return new Queue(ORDER_TIMEOUT_QUEUE,true,false,false,args);
    }

    @Bean
    public Binding orderBindingExchange(){
        return BindingBuilder.bind(ORDER_TIMEOUT_QUEUE()).to(ORDER_TIMEOUT_EXCHANGE()).with(ORDER_TIMEOUT_KEY);
    }



}
