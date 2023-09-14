package com.cxb.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信验证码请求
 *
 * @author cxb
 */
@Data
public class SmsCaptchaRequest implements Serializable {

    /**
     * 手机号
     */
    String phoneNumber;

    /**
     * 验证码类型
     */
    String captchaType;
}
