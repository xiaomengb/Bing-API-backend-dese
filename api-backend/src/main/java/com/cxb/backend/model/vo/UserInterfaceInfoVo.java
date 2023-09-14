package com.cxb.backend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author cxb
 */
@Data
public class UserInterfaceInfoVo implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 调用用户Id
     */
    private Long userId;

    /**
     * 接口Id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 0-正常 ，1-禁用
     */
    private Integer interfaceStatus;

    /**
     * 0-正常 ，1-禁用
     */
    private Integer status;


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
     * 价格
     */
    private Double price;


    /**
     * 请求类型
     */
    private String method;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
