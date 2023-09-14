package com.cxb.backend.controller;


import com.cxb.apicommon.common.BaseResponse;
import com.cxb.apicommon.common.ResultUtils;
import com.cxb.backend.model.vo.InvokeInterfaceInfoVO;
import com.cxb.backend.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cxb
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;


    @GetMapping("/top/interface/invoke")
    BaseResponse<List<InvokeInterfaceInfoVO>> listTopInvokeInterfaceInfo () {
        List<InvokeInterfaceInfoVO> listTopInvokeInterfaceInfo = chartService.listTopInvokeInterfaceInfo(3);
        return ResultUtils.success(listTopInvokeInterfaceInfo);
    }

}
