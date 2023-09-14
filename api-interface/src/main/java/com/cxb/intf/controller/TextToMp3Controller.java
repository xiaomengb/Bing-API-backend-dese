package com.cxb.intf.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.other.ApiResult;
import com.cxb.apicommon.other.ApiResultUtils;
import com.cxb.apicommon.common.ErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 语音合成
 *
 * @author cxb
 */
@RestController
@RequestMapping("/mp3")
public class TextToMp3Controller {

    private static final String TTM_API = "https://api.a20safe.com/api.php?api=8&key=b72bc48c0dd9fada3f7cd8200f710ae7&text=";


    @PostMapping("/ttm")
    public ApiResult TextToMp3(String text,
                               @RequestParam(required = false) String speed,
                               @RequestParam(required = false) String speaker) {
        if (CharSequenceUtil.hasBlank(text)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        StringBuilder sb = new StringBuilder(TTM_API);
        sb.append(text);
        if (CharSequenceUtil.isNotBlank(speed)) {
            if (Integer.parseInt(speaker) < -3 || Integer.parseInt(speaker) > 3) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            sb.append("&spd=").append(speed);
        }
        if (CharSequenceUtil.isNotBlank(speaker)) {
            if (Integer.parseInt(speaker) < 1 || Integer.parseInt(speaker) > 3) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            sb.append("&speaker=").append(speaker);
        }

        HttpResponse response = HttpRequest.get(String.valueOf(sb))
                .timeout(50000)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //必须忽略空值
        JSONObject obj = JSONUtil.parseObj(response.body(), true);
        Object data = obj.getJSONArray("data").get(0);
        if (data == null ) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ApiResultUtils.success(data);
    }

}
