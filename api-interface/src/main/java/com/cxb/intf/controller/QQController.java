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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * QQ有关接口
 *
 * @author cxb
 */
@RestController
@RequestMapping("/qq")
@Slf4j
public class QQController {

    private static final String QQ_TRANSLATE_API = "https://api.oioweb.cn/api/txt/QQFanyi";

    @PostMapping("/translate")
    public ApiResult translateText(String sourceText) {
        if (CharSequenceUtil.isBlank(sourceText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HashMap<String, Object> param = new HashMap<>();
        param.put("sourceText", sourceText);
        HttpResponse response = HttpRequest.post(QQ_TRANSLATE_API)
                .timeout(50000)
                .form(param)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        JSONObject obj = JSONUtil.parseObj(response.body(), true);
        Object data = obj.get("result");
        if (data == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ApiResultUtils.success(data);
    }
}
