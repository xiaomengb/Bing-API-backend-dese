package com.cxb.apicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 订单
 * @TableName order
 */
@TableName(value ="api_order")
@Data
public class Order implements Serializable {
    /**
     * 订单id
     */
    @TableId
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
     * 购买数量
     */
    private Integer count;

    /**
     * 订单应付价格
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态 0-未支付 1-已支付 2-超时支付
     */
    private Integer status;

    /**
     * 单价（元/条）
     */
    private Double price;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}