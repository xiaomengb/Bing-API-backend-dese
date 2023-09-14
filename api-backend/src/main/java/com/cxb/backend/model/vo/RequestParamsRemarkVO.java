package com.cxb.backend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求参数和备注视图
 * @author cxb
 */
@Data
public class RequestParamsRemarkVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否必须
     */
    private String isRequired;

    /**
     * 类型
     */
    private String type;

    /**
     * 说明
     */
    private String remark;
}
