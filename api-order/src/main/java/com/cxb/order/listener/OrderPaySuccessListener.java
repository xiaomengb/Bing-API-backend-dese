package com.cxb.order.listener;


import cn.hutool.core.text.CharSequenceUtil;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.Order;
import com.cxb.apicommon.service.ApiBackendService;
import com.cxb.order.enums.OrderStatusEnum;
import com.cxb.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;


@Component
@Slf4j
public class OrderPaySuccessListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private OrderService orderService;

    @DubboReference
    private ApiBackendService apiBackendService;

    private static final String CONSUME_ORDER_PAY_SUCCESS_INFO = "consume:order:pay:success:";


    //监听订单交易成功队列，实现订单状态的修改以及给用户分配购买的接口调用次数
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = ORDER_DONE_QUEUE, durable = "true", autoDelete = "false", arguments = {
                    @Argument(name = "x-dead-letter-exchange", value = DEAD_ORDER_DONE_EXCHANGE),
                    @Argument(name = "x-dead-letter-routing-key", value = DEAD_ORDER_DONE_KEY),
                    @Argument(name = "x-message-ttl", value = "15", type = "java.lang.Integer")
            }),
            exchange = @Exchange(value = ORDER_DONE_EXCHANGE),
            key = ORDER_DONE_KEY
    )}, ackMode = "MANUAL")
    public void receiveOrderPaySuccess(Order order,Message message, Channel channel){

        //1.监听到消息
        Long orderId = order.getId();
        log.info("监听到订单支付成功消息,订单号: {}", orderId);

        boolean success = false;
        int retryCount = 3;

        while (!success && retryCount-- > 0) {
            try {
                //2.消费端的消息幂等性问题，因为消费端开启手动确认机制，会有消息重复消费的问题，这里使用redis记录已经成功处理的订单来解决(消费端的消息可靠机制)
                String recordOrderId = stringRedisTemplate.opsForValue().get(CONSUME_ORDER_PAY_SUCCESS_INFO + orderId);

                if (CharSequenceUtil.isNotBlank(recordOrderId)) {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    return;
                }

                //3.修改订单状态
                boolean b = orderService.lambdaUpdate()
                        .eq(Order::getId, orderId)
                        .set(Order::getStatus, OrderStatusEnum.DONE.getValue())
                        .update();
                if (!b) {
                    log.error("更新支付状态失败！！！");
                }
                //4.给用户分配购买的接口调用次数

                Order dbOrder = orderService.lambdaQuery().eq(Order::getId, orderId).one();
                if (dbOrder == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单号不存在");
                }

                Long userId = dbOrder.getUserId();
                Long interfaceId = dbOrder.getInterfaceId();
                Integer count = dbOrder.getCount();

                boolean updateInvokeCount = apiBackendService.updateUserInterfaceInvokeCount(userId, interfaceId, count);

                if (!b || !updateInvokeCount) {
                    log.error("更新用户接口调用次数失败！！！");
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    return;
                }

                //5.为解决消费端的消息幂等性问题，记录已经的成功处理的消息。15分钟后订单已经结束，淘汰记录的订单消息
                stringRedisTemplate.opsForValue().set(CONSUME_ORDER_PAY_SUCCESS_INFO + orderId, "1", 15, TimeUnit.MINUTES);

                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                e.printStackTrace();
                //设置事务回滚
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                } catch (IOException ex) {
                    e.printStackTrace();
                    //设置事务回滚
                    TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
                }
            }

        }
        // 达到最大重试次数后仍然回滚失败
        if (!success) {
            // 手动删除
            try {
                log.error("处理支付成功消息失败！！！");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException e) {
                e.printStackTrace();
                //设置事务回滚
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
            }
        }
    }


    /**
     * 监听支付成功死信队列消息
     *
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = DEAD_ORDER_DONE_QUEUE, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = DEAD_ORDER_DONE_EXCHANGE),
            key = DEAD_ORDER_DONE_KEY
    ))
    public void receiveOrderPaySuccessDead(Order order,Message message, Channel channel) throws IOException {
        Long orderId = order.getId();
        log.info("支付成功死信队列监听到消息,订单号：{}", orderId);

        //修改订单状态
        boolean b = orderService.lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, OrderStatusEnum.FAILURE.getValue())
                .update();
        if(!b){
            log.error("支付成功死信队列，修改订单状态失败");
        }

    }


}