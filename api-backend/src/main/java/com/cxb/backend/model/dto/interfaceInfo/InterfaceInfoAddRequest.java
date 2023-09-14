package com.cxb.backend.model.dto.interfaceInfo;

import com.cxb.backend.model.vo.RequestParamsRemarkVO;
import com.cxb.backend.model.vo.ResponseParamsRemarkVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应参数
     */
    private String responseParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 参数示例
     */
    private String parameterExample;

    /**
     * 请求参数说明
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * 响应参数说明
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 价格
     */
    private Double price;

    /**
     * 付费接口库存
     */
    private Integer stock;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;



    private static final long serialVersionUID = 1L;
}