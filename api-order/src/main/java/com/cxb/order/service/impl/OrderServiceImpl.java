package com.cxb.order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.common.JwtUtils;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.InterfaceInfo;
import com.cxb.apicommon.model.entity.Order;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.service.ApiBackendService;
import com.cxb.apicommon.service.InnerUserService;
import com.cxb.order.enums.OrderStatusEnum;
import com.cxb.order.mapper.OrderMapper;
import com.cxb.order.model.dto.OrderAddRequest;
import com.cxb.order.model.dto.OrderQueryRequest;
import com.cxb.order.model.vo.OrderVo;
import com.cxb.order.service.OrderService;
import com.cxb.order.utils.IdWorkerUitil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.cxb.apicommon.constant.RabbitmqConstant.*;

/**
 *
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {


    @DubboReference
    private InnerUserService innerUserService;


    @DubboReference
    private ApiBackendService apiBackendService;

    @Resource
    private IdWorkerUitil idWorkerUitil;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 添加订单
     *
     * @param orderAddRequest
     * @param request
     * @return
     */
    @Override
    public boolean addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request) {

        //1.校验参数、用户、接口
        Long userId = orderAddRequest.getUserId();
        Long interfaceId = orderAddRequest.getInterfaceId();
        Integer count = orderAddRequest.getCount();
        BigDecimal totalAmount = orderAddRequest.getTotalAmount();
        Double price = orderAddRequest.getPrice();

        if (userId == null || interfaceId == null || count == null || totalAmount == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (count <= 0 || totalAmount.compareTo(new BigDecimal(0)) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = innerUserService.getLoginUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        InterfaceInfo interfaceInfo = apiBackendService.getInterfaceInfoById(interfaceId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口不存在");
        }

        //2.后端校验价格
        double accountPrice = interfaceInfo.getPrice() * count;
        //保留两位小数
        double doubleValue = new BigDecimal(Double.toString(accountPrice)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (doubleValue != totalAmount.doubleValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格错误");
        }

        //3.查询接口库存
        int stock = apiBackendService.getPayInterfaceStock(interfaceId);
        if (stock <= 0 || stock < count) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口库存不足");
        }

        //4.扣减库存
        boolean b = apiBackendService.updatePayInterfaceStock(interfaceId, count);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扣减库存失败");
        }


        //5.数据库保存订单数据
        //生成订单号
        long orderId = idWorkerUitil.nextId("order");
        Order order = BeanUtil.copyProperties(orderAddRequest, Order.class);
        order.setId(orderId);
        boolean ob = save(order);
        if (!ob) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        //6.发送消息到队列
        rabbitTemplate.convertAndSend(ORDER_TIMEOUT_EXCHANGE, ORDER_TIMEOUT_KEY, order);
        return true;
    }

    @Override
    public Page<OrderVo> listOrderVoByPage(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();
        Long userId = orderQueryRequest.getUserId();
        Integer type = orderQueryRequest.getType();
        if (current < 0 || pageSize < 0 || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!OrderStatusEnum.getValues().contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long jwtUserId = JwtUtils.getUserIdByToken(request);
        if (!Objects.equals(jwtUserId, userId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户错误");
        }

        User user = innerUserService.getLoginUser(userId);
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        Page<Order> page = lambdaQuery().eq(Order::getUserId, user.getId())
                .eq(Order::getStatus, type)
                .page(new Page<>(current, pageSize));
        Page<OrderVo> orderVoPage = new Page<>(current, pageSize, page.getTotal());

        List<OrderVo> orderVoList = page.getRecords().stream().map(order -> {
            InterfaceInfo interfaceInfo = apiBackendService.getInterfaceInfoById(order.getInterfaceId());
            OrderVo orderVo = BeanUtil.copyProperties(order, OrderVo.class);
            orderVo.setName(interfaceInfo.getName());
            return orderVo;
        }).collect(Collectors.toList());

        orderVoPage.setRecords(orderVoList);
        return orderVoPage;
    }
}




