package com.cxb.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxb.apicommon.common.BaseResponse;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.common.ResultUtils;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.Order;
import com.cxb.order.model.dto.OrderAddRequest;
import com.cxb.order.model.dto.OrderQueryRequest;
import com.cxb.order.model.vo.OrderVo;
import com.cxb.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author cxb
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request) {

        if (orderAddRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = orderService.addOrder(orderAddRequest, request);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加订单失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    public BaseResponse<Page<OrderVo>> listOrderVoByPage(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        if (orderQueryRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<OrderVo> orderVoPage = orderService.listOrderVoByPage(orderQueryRequest, request);
        return ResultUtils.success(orderVoPage);
    }

}
