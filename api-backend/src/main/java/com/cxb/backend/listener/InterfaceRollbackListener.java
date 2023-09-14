package com.cxb.backend.listener;

import com.cxb.backend.service.UserInterfaceInfoService;
import com.cxb.apicommon.model.dto.message.UserInterfaceMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


import java.io.IOException;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;

/**
 * 接口调用回滚监听器
 *
 * @author cxb
 */
@Component
@Slf4j
public class InterfaceRollbackListener {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = API_INTERFACE_QUEUE, durable = "true", autoDelete = "false"),
            exchange = @Exchange(name = API_INTERFACE_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = API_INTERFACE_ROLLBACK_KEY
    ),ackMode = "MANUAL")//手动确认
    /*回滚接口统计*/
    public void interfaceRollback(UserInterfaceMessage userInterfaceMessage, Message message, Channel channel) throws IOException {
        boolean success = false;
        int retryCount = 3;
        Long interfaceId = userInterfaceMessage.getInterfaceId();
        Long userId = userInterfaceMessage.getUserId();
        while (!success && retryCount-- > 0){
            try {
                // 处理消息
                log.info("收到消息: {}, deliveryTag = {}", userInterfaceMessage, message.getMessageProperties().getDeliveryTag());
                // 正常处理完毕，手动确认
                boolean b = userInterfaceInfoService.rollbackInvokeCount(userId, interfaceId);
                if(b) {
                    success = true;
                    //deliveryTag: 相当于消息的唯一标识，用于 mq 辨别是哪个消息被 ack/nak
                    //第二个参数返回true，表示需要将这条消息投递给其他的消费者重新消费
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }catch (Exception e){
                log.error("程序异常：{}", e.getMessage());
            }
        }
        // 达到最大重试次数后仍然消费失败
        if(!success){
            // 手动删除
            //todo 设置死信队列，移入队列人工操作
            try {
                log.error("接口次数调用回滚失败");
                // 第三个参数true，表示这个消息会重新进入队列(我们手动重试，避免有异常导致循环)
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

