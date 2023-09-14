package com.cxb.backend.model.vo;

import com.cxb.apicommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图表接口视图类
 *
 * @author cxb
 */
@EqualsAndHashCode(callSuper = true)//不加此注解，不会比较父类属性
@Data
public class InvokeInterfaceInfoVO extends InterfaceInfo {

    /**
     * 调用次数
     */
    private Integer invokeNum;

    private static final long serialVersionUID = 1L;

}
