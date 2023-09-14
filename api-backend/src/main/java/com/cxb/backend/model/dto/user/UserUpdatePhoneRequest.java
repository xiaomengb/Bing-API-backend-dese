package com.cxb.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author cxb
 */
@Data
public class UserUpdatePhoneRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 验证码
     */
    private String captcha;

}
