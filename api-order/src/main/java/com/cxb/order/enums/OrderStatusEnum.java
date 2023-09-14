package com.cxb.order.enums;

import com.cxb.apicommon.common.ResultUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 *
 * @author cxb
 */
public enum OrderStatusEnum {

    TOBEPAID("待支付",0),

    DONE("已支付",1),

    TIMEOUT("支付超时",2),

    FAILURE("出库失败",3);



    private final String text;

    private final int value;

    OrderStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<Integer> getValues(){
        return Arrays.stream(OrderStatusEnum.values()).map(item-> item.value).collect(Collectors.toList());
    }
}
