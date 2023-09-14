package com.cxb.apicommon.constant;

/**
 * @author cxb
 */
public interface RabbitmqConstant {

    /*api接口 */
    /**
     * 接口队列
     */
    String API_INTERFACE_QUEUE = "api.interface.queue";
    /**
     * 接口交换机
     */
    String API_INTERFACE_EXCHANGE = "api.interface.exchange";
    /**
     * 调用接口回滚 RoutingKey
     */
    String API_INTERFACE_ROLLBACK_KEY = "api.interface.rollback.key";

    /*短信验证码*/
    /**
     * 短信队列
     */
    String SMS_CAPTCHA_QUEUE = "api.sms.queue";
    /**
     * 短信交换机
     */
    String SMS_CAPTCHA_EXCHANGE = "api.sms.exchange";
    /**
     * 短信 RoutingKey
     */
    String SMS_CAPTCHA_KEY = "api.sms.key";

    /*订单超时*/
    /**
     * 订单超时队列
     */
    String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    /**
     * 订单超时交换机
     */
    String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
    /**
     * 订单超时 RoutingKey
     */
    String ORDER_TIMEOUT_KEY = "order.timeout.key";

    /**
     * 订单超时死信队列
     */
    String DEAD_ORDER_TIMEOUT_QUEUE = "dead.order.timeout.queue";
    /**
     * 订单超时死信交换机
     */
    String DEAD_ORDER_TIMEOUT_EXCHANGE = "dead.order.timeout.exchange";
    /**
     * 订单超时死信 RoutingKey
     */
    String DEAD_ORDER_TIMEOUT_KEY = "dead.order.timeout.key";


    
    /*订单支付成功*/
    /**
     * 订单支付成功队列
     */
    String ORDER_DONE_QUEUE = "order.done.queue";
    /**
     * 订单支付成功交换机
     */
    String ORDER_DONE_EXCHANGE = "order.done.exchange";
    /**
     * 订单支付成功 RoutingKey
     */
    String ORDER_DONE_KEY = "order.done.key";

    /**
     * 订单支付成功死信队列
     */
    String DEAD_ORDER_DONE_QUEUE = "dead.order.done.queue";
    /**
     * 订单支付成功死信交换机
     */
    String DEAD_ORDER_DONE_EXCHANGE = "dead.order.done.exchange";
    /**
     * 订单支付成功死信 RoutingKey
     */
    String DEAD_ORDER_DONE_KEY = "dead.order.done.key";

}
