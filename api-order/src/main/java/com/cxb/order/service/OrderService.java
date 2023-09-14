package com.cxb.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxb.apicommon.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cxb.order.model.dto.OrderAddRequest;
import com.cxb.order.model.dto.OrderQueryRequest;
import com.cxb.order.model.vo.OrderVo;

import javax.servlet.http.HttpServletRequest;

/**
*
*/
public interface OrderService extends IService<Order> {

    boolean addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request);

    Page<OrderVo> listOrderVoByPage(OrderQueryRequest orderQueryRequest, HttpServletRequest request);
}
