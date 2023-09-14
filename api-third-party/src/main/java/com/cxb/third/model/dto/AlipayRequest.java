package com.cxb.third.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author niuma
 * @create 2023-05-06 9:06
 */
@Data
public class AlipayRequest implements Serializable {
    private static final long serialVersionUID = -8597630489529830444L;

    /**
     * 对应接口订单号
     */
    private Long tradeNo;

    /**
     * 总价
     */
    private double totalAmount;

    /**
     * 项目名
     */
    private String subject;

    /**
     * 支付宝交易凭证号
     */
    private String alipayTradeNo;
}
