package com.cxb.backend.provider;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.backend.mapper.InterfaceInfoMapper;
import com.cxb.backend.mapper.UserInterfaceInfoMapper;
import com.cxb.apicommon.model.entity.InterfaceInfo;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;
import com.cxb.apicommon.service.ApiBackendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 作为服务提供方，提供远程调用接口
 */
@DubboService
@Slf4j
public class ApiBackendServiceImpl implements ApiBackendService {


    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;


    @Override
    public InterfaceInfo getInvokeInterfaceInfo(String url, String method) {
        if (CharSequenceUtil.hasBlank(url, method)) {
            log.error("调用的接口不存在");
        }
        LambdaQueryWrapper<InterfaceInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(InterfaceInfo::getUrl, url).eq(InterfaceInfo::getMethod, method);
        return interfaceInfoMapper.selectOne(lambdaQueryWrapper);
    }


    @Override
    public boolean hasInvokeNum(long userId, long interfaceInfoId) {
        if (userId < 0 || interfaceInfoId < 0) {
            log.error("调用的接口不存在");
        }
        LambdaQueryWrapper<UserInterfaceInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .gt(UserInterfaceInfo::getLeftNum, 0);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(lqw);
        return userInterfaceInfo != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean[] invokeInterfaceCount(long userId, long interfaceInfoId) {
        Boolean[] arr = new Boolean[2];
        //校验用户的接口剩余调用次数是否充足
        //接口总调用次数+1，剩余调用次数-1
        if (userId < 0 || interfaceInfoId < 0) {
            log.error("调用的接口不存在");
        }
        boolean b = this.hasInvokeNum(userId, interfaceInfoId);
        if (!b) {
            log.error("接口剩余调用次数不足");
            return arr;
        }
        LambdaUpdateWrapper<UserInterfaceInfo> luw = new LambdaUpdateWrapper<>();
        luw.eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .gt(UserInterfaceInfo::getLeftNum, 0)
                .setSql("leftNum = leftNum -1, totalNum = totalNum + 1");
        int update = userInterfaceInfoMapper.update(null, luw);
        if (update <= 0) {
            log.error("接口调用次数统计数据库操作失败");
            return arr;
        }
        arr[0] = true;
        arr[1] = true;
        return arr;
    }

    @Override
    public InterfaceInfo getInterfaceInfoById(Long interfaceId) {
        if (interfaceId == null || interfaceId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoMapper.selectById(interfaceId);
    }

    @Override
    public int getPayInterfaceStock(Long interfaceId) {
        if (interfaceId == null || interfaceId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口不存在");
        }
        return interfaceInfo.getStock();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePayInterfaceStock(Long interfaceId, int num) {
        //查询接口
        int stock = getPayInterfaceStock(interfaceId);
        if (stock <= 0 || stock < num) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口库存不足");
        }
        //扣减库存
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceId);
        interfaceInfo.setStock(stock - num);
        int b = interfaceInfoMapper.updateById(interfaceInfo);
        if (b <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        return true;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackPayInterfaceStock(Long interfaceId, Integer count) {
        if (interfaceId == null || interfaceId < 0 || count == null || count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //回滚库存
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceId);
        interfaceInfo.setStock(interfaceInfo.getStock() + count);
        int b = interfaceInfoMapper.updateById(interfaceInfo);
        if (b <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    public boolean updateUserInterfaceInvokeCount(Long userId, Long interfaceId, Integer count) {
        if (userId == null || interfaceId == null || count == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<UserInterfaceInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceId);
        UserInterfaceInfo one = userInterfaceInfoMapper.selectOne(lqw);
        if (one == null) {
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceId);
            userInterfaceInfo.setLeftNum(count);
            int insert = userInterfaceInfoMapper.insert(userInterfaceInfo);
            if (insert <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return true;
        }
        one.setLeftNum(one.getLeftNum() + count);
        int i = userInterfaceInfoMapper.updateById(one);
        if (i <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }


}
