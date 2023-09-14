package com.cxb.order.listener;

import com.cxb.apicommon.model.entity.Order;
import com.cxb.apicommon.service.ApiBackendService;
import com.cxb.order.enums.OrderStatusEnum;
import com.cxb.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;


import java.io.IOException;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;

/**
 * 订单超时监听器
 *
 * @author cxb
 */
@Component
@Slf4j
public class OrderTimeoutListener {

    @Resource
    private OrderService orderService;

    @DubboReference
    private ApiBackendService apiBackendService;

    //死信队列，实现支付超时的回滚功能
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = DEAD_ORDER_TIMEOUT_QUEUE, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = DEAD_ORDER_TIMEOUT_EXCHANGE),
            key = DEAD_ORDER_TIMEOUT_KEY
    ),ackMode = "MANUAL")
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrderTimeOut(Order order, Message message, Channel channel){

        log.info("监听到订单超时消息: {}", order);

        boolean success = false;
        int retryCount = 3;

        while (!success && retryCount-- > 0) {
            Order dbOrder = orderService.getById(order.getId());
            //根据订单状态判断订单是否支付成功，如果没有支付成功则回滚
            if (dbOrder.getStatus().equals(OrderStatusEnum.TOBEPAID.getValue())) {
                Long interfaceId = order.getInterfaceId();
                Integer count = order.getCount();
                try {
                    boolean b = apiBackendService.rollbackPayInterfaceStock(interfaceId, count);
                    if (!b) {
                        log.error("回滚库存失败!!!");
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    }
                    boolean ob = orderService.lambdaUpdate()
                            .eq(Order::getId, dbOrder.getId())
                            .set(Order::getStatus, OrderStatusEnum.TIMEOUT.getValue())
                            .update();
                    if (!ob) {
                        log.error("设置订单超时状态失败!!!");
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    }
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    success = true;
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
        }
        // 达到最大重试次数后仍然回滚失败
        if (!success) {
            // 手动删除
            try {
                log.error("处理超时订单失败！！！");
                // 第三个参数true，表示这个消息会重新进入队列(我们手动重试，避免有异常导致循环)
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException e) {
                e.printStackTrace();
                //设置事务回滚
                TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
            }
        }
    }

}
