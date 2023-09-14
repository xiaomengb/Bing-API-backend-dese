package com.cxb.order.model.dto;

import com.cxb.apicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;


/**
 * @author cxb
 */
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {

    /**
     * 订单状态类型
     */
   private Integer type;

    /**
     * 用户id
     */
   private Long userId;


}
