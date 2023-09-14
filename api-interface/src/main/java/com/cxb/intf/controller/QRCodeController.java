package com.cxb.intf.controller;

import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.other.ApiResult;
import com.cxb.apicommon.other.ApiResultUtils;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.intf.util.RedirectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;

/**
 * 二维码相关接口
 *
 * @author cxb
 */
@RestController
@RequestMapping("/qr")
@Slf4j
public class QRCodeController {

    private static final String QR_CREATE_API = "https://api.oioweb.cn/api/qrcode/encode";

    @PostMapping("/create")
    public ApiResult createQRCode(String text,
                                  @RequestParam(required = false) Double m,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) Double size) throws IOException {
        StringBuilder url = new StringBuilder(QR_CREATE_API);
        url.append("?text=").append(text);
        if (m != null) {
            if (m < 0 || m > 10) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            url.append("&").append(m);
        }
        if (type != null) {
            if (!type.equals("jpg") && !type.equals("svg")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            url.append("&").append(type);
        }
        if (size != null) {
            if (size < 5 || size > 30) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            url.append("&").append(size);
        }
        String redirectUrl = RedirectUtil.getRedirectUrl(String.valueOf(url));
        if (redirectUrl == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("orImageUrl", redirectUrl);
        return ApiResultUtils.success(map);
    }
}
