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
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Ai 聊天机器人
 *
 * @author cxb
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    private static final String AI_CHAT_API = "https://api.oioweb.cn/api/ai/chat";


    private static final String AI_QA_API = "https://api.a20safe.com/api.php?api=51&key=b72bc48c0dd9fada3f7cd8200f710ae7&text=";



    @PostMapping("/chat")
    public ApiResult aiChat(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        HashMap<String, Object> param = new HashMap<>();
        param.put("text", text);
        HttpResponse response = HttpRequest.post(AI_CHAT_API)
                .timeout(50000)
                .form(param)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //必须忽略空值
        Map<String, Object> map = JSONUtil.parseObj(response.body(),true);
        Object data = map.get("result");
        if (data == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ApiResultUtils.success(data);
    }

    /**
     * 问答ai
     * @return
     */
    @PostMapping("/qa")
    public ApiResult qaAi(String text){
        if(CharSequenceUtil.isBlank(text)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String url = AI_QA_API + text;
        HttpResponse response = HttpRequest.get(url)
                .timeout(50000)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //必须忽略空值
        JSONObject obj = JSONUtil.parseObj(response.body(), true);
        Object data = obj.getJSONArray("data").get(0);
        if (data == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ApiResultUtils.success(data);
    }
}
