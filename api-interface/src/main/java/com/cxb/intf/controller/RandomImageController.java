package com.cxb.intf.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.other.ApiResult;
import com.cxb.apicommon.other.ApiResultUtils;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.intf.util.RedirectUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

/**
 * 随即头像接口
 *
 * @author cxb
 */
@RestController
@RequestMapping("/random/image")
public class RandomImageController {

    private static final String RANDOM_IMAGE_API = "https://www.loliapi.com/acg/";

    @GetMapping("/{name}")
    public ApiResult getRandomImage(@PathVariable(value = "name") String name) throws IOException {
        String url;
        if (CharSequenceUtil.isBlank(name)) {
            url = RANDOM_IMAGE_API;
        } else if (name.equals("pc")) {
            url = RANDOM_IMAGE_API + "/pc";
        } else if (name.equals("pe")) {
            url = RANDOM_IMAGE_API + "/pe";
        } else if (name.equals("pp")) {
            url = RANDOM_IMAGE_API + "/pp";
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String redirectUrl = RedirectUtil.getRedirectUrl(url);
        if (redirectUrl == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("acgImageUrl", redirectUrl);
        return ApiResultUtils.success(map);
    }

    @GetMapping("/")
    public ApiResult getRandomImage() throws IOException {
        String redirectUrl = RedirectUtil.getRedirectUrl(RANDOM_IMAGE_API);
        if (redirectUrl == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("acgImageUrl", redirectUrl);
        return ApiResultUtils.success(map);
    }



}
