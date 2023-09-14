package com.cxb.third.listener;


import cn.hutool.json.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.cxb.apicommon.model.dto.message.SmsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;

/**
 * 短信验证码消息监听
 * @author cxb
 */
@Component
@Slf4j
public class SmsCaptchaListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = SMS_CAPTCHA_QUEUE,durable = "true",autoDelete = "true"),
            exchange = @Exchange(value = SMS_CAPTCHA_EXCHANGE,type = ExchangeTypes.TOPIC),
            key = SMS_CAPTCHA_KEY
    ))
    public void sendSmsCaptcha(SmsMessage smsMessage) throws Exception {
        String captcha = smsMessage.getCaptcha();
        String phoneNumber = smsMessage.getPhoneNumber();
        String captChaType = smsMessage.getCaptChaType();

        try {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId("#")
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret("#");
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        Client client = new Client(config);

            JSONObject jsonObject = new JSONObject();
            jsonObject.set("code",captcha);
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName("BingApi")
                .setTemplateCode("SMS_285635016")
                .setTemplateParam(jsonObject.toString());
        RuntimeOptions runtime = new RuntimeOptions();

        // 复制代码运行请自行打印 API 的返回值
        SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
        if(!sendSmsResponse.getBody().code.equals("OK")){
            log.error("短信发送响应错误: {}",sendSmsResponse.getBody().message);
        }
        stringRedisTemplate.opsForValue().set("api:sms:" + captChaType + ":" + phoneNumber,
                captcha,5, TimeUnit.MINUTES);
        } catch (TeaException error) {
            // 如有需要，请打印 error
            log.error(error.message);
            com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception e) {
            TeaException error = new TeaException(e.getMessage(), e);
            // 如有需要，请打印 error
            log.error(e.getMessage());
            com.aliyun.teautil.Common.assertAsString(error.message);
        }

    }

}
