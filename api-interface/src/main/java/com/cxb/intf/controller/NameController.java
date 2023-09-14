package com.cxb.intf.controller;



import cn.hutool.core.text.CharSequenceUtil;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.other.ApiResult;
import com.cxb.apicommon.other.ApiResultUtils;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * API 查询名称接口
 *
 * @author cxb
 */

@RestController
@RequestMapping("/test")
@Slf4j
public class NameController {

    @GetMapping("/get/{name}")
    public ApiResult getName(@PathVariable String name, HttpServletRequest request) {
        if(CharSequenceUtil.isBlank(name)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ApiResultUtils.success("你的名字是 " + name);
    }

}
