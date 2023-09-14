package com.cxb.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单视图类
 *
 * @author cxb
 */
@Data
public class OrderVo implements Serializable {


    /**
     * 订单id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 接口名
     */
    private String name;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单应付价格
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态 0-未支付 1-已支付 2-超时支付 3-出库失败
     */
    private Integer status;

    /**
     * 单价（元/条）
     */
    private BigDecimal price;

    /**
     * 创建时间
     */
    private Date createTime;

}
