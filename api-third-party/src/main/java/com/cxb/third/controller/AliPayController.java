package com.cxb.third.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.Order;
import com.cxb.third.config.AliPayConfig;
import com.cxb.third.model.domain.AlipayInfo;
import com.cxb.third.model.dto.AlipayRequest;
import com.cxb.third.service.AlipayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cxb.apicommon.constant.RabbitmqConstant.ORDER_DONE_EXCHANGE;
import static com.cxb.apicommon.constant.RabbitmqConstant.ORDER_DONE_KEY;

/**
 * @author cxb
 */
@RestController
@RequestMapping("/alipay")
@Slf4j
public class AliPayController {

    private static final String ALIPAY_TRADE_SUCCESS_RECORD = "alipay:trade:success:record:";
    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private AlipayInfoService alipayInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/pay") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public void pay(AlipayRequest alipayRequest, HttpServletResponse httpResponse) throws Exception {
        //1.校验参数
        String outTradeNo = String.valueOf(alipayRequest.getTradeNo());
        String subject = alipayRequest.getSubject();
        double totalAmount = alipayRequest.getTotalAmount();
        if (CharSequenceUtil.hasBlank(outTradeNo, subject) || totalAmount < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(
                aliPayConfig.getGatewayUrl(),
                aliPayConfig.getAppId(),
                aliPayConfig.getPrivateKey(),
                aliPayConfig.getFormat(),
                aliPayConfig.getCharset(),
                aliPayConfig.getPublicKey(),
                aliPayConfig.getSignType());

        // 3. 创建 Request并设置Request参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
        //回调地址和返回页面地址
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setReturnUrl(aliPayConfig.getReturnUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", alipayRequest.getTradeNo());  // 订单编号
        bizContent.set("total_amount", alipayRequest.getTotalAmount()); // 订单的总金额
        bizContent.set("subject", alipayRequest.getSubject());   // 支付的名称
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        request.setBizContent(bizContent.toString());


        // 4.执行请求，拿到响应的结果，返回给浏览器
        String form = null;
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (form == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("响应支付详情：{}", form);
        httpResponse.setContentType("text/html;charset=" + aliPayConfig.getCharset());
        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }


    /**
     * 支付成功回调,注意这里必须是POST接口
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/notify")
    public synchronized void payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            // 支付宝验签
            if (AlipaySignature.rsaCheckV1(params, aliPayConfig.getPublicKey(), aliPayConfig.getCharset(), aliPayConfig.getSignType())) {
                //验证成功
                log.info("支付成功:{}", params);
                // 幂等性保证：判断该订单号是否被处理过，解决因为多次重复收到阿里的回调通知导致的订单重复处理的问题
                Object outTradeNo = stringRedisTemplate.opsForValue().get(ALIPAY_TRADE_SUCCESS_RECORD + params.get("out_trade_no"));
                if (null == outTradeNo) {
                    // 验签通过，将订单信息存入数据库
                    AlipayInfo alipayInfo = new AlipayInfo();
                    alipayInfo.setSubject(params.get("subject"));
                    alipayInfo.setTradeStatus(params.get("trade_status"));
                    alipayInfo.setTradeNo(params.get("trade_no"));
                    alipayInfo.setOrderNumber(Long.valueOf(params.get("out_trade_no")));
                    alipayInfo.setTotalAmount(BigDecimal.valueOf(Double.parseDouble(params.get("total_amount"))));
                    alipayInfo.setBuyerId(params.get("buyer_id"));
                    alipayInfo.setGmtPayment(DateUtil.parse(params.get("gmt_payment")));
                    alipayInfo.setBuyerPayAmount(BigDecimal.valueOf(Double.parseDouble(params.get("buyer_pay_amount"))));
                    alipayInfoService.save(alipayInfo);
                    //记录处理成功的订单，实现订单幂等性
                    stringRedisTemplate.opsForValue().set(ALIPAY_TRADE_SUCCESS_RECORD + alipayInfo.getOrderNumber(), "1", 15, TimeUnit.MINUTES);
                    //发送支付成功消息
                    sendOrderPaySuccess(params.get("out_trade_no"));
                }
            }
        }
    }

    /**
     * @param outTradeNo 的订单号
     */
    public void sendOrderPaySuccess(String outTradeNo) {

        Order order = new Order();
        order.setId(Long.valueOf(outTradeNo));
        rabbitTemplate.convertAndSend(ORDER_DONE_EXCHANGE, ORDER_DONE_KEY,order);
        log.info("消息队列给订单服务发送支付成功消息，订单号：{}", outTradeNo);
    }


}
