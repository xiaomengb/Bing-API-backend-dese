package com.cxb.apicommon.model.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 短信验证码消息
 *
 * @author cxb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsMessage implements Serializable {

    /**
     * 验证码
     */
    String captcha;

    /**
     * 验证码类型
     */
    String captChaType;


    /**
     * 手机号
     */
    String phoneNumber;
}
