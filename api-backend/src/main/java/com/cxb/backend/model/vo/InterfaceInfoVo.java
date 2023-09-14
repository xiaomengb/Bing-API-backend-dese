package com.cxb.backend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口信息视图
 */
@Data
public class InterfaceInfoVo implements Serializable {
    /**
     * 主键
     */
    private Long id;

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
     * 请求参数说明
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * 响应参数说明
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    /**
     * 参数示例
     */
    private String parameterExample;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * sdk客户端方法名
     */
    private String clientMethod;

    /**
     * sdk客户端请求对象
     */
    private String requestObject;

    /**
     * sdk客户端路径
     */
    private String sdk;

    /**
     * 价格
     */
    private Double price;

    /**
     * 付费接口库存
     */
    private Integer stock;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}