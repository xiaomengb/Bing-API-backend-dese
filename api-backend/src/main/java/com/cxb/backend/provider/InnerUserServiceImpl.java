package com.cxb.backend.provider;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cxb.apicommon.common.ErrorCode;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.service.InnerUserService;
import com.cxb.backend.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getInvokeUser(String accessKey) {
        if (CharSequenceUtil.isBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getAccessKey, accessKey);
        return userMapper.selectOne(lambdaQueryWrapper);
    }


    @Override
    public User getLoginUser(Long userId) {
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getId, userId);
        return userMapper.selectOne(lambdaQueryWrapper);
    }

}
