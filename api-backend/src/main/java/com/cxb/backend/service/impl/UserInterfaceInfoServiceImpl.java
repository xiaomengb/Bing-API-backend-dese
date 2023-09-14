package com.cxb.backend.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxb.backend.mapper.UserInterfaceInfoMapper;
import com.cxb.backend.model.vo.UserInterfaceInfoVo;
import com.cxb.backend.service.UserInterfaceInfoService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;
import com.cxb.apicommon.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 14105
 * @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service实现
 * @createDate 2023-08-10 12:12:26
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;


    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {

        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //创建时，所有参数必须为空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在！");
            }
        }

        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余接口次数不能小于0");
        }

    }

    /**
     * 统计调用次数
     *
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        //判断
        if (userId <= 0 || interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return lambdaUpdate()
                .setSql("totalNum = totalNum + 1 ,leftNum = leftNum -1")
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .gt(UserInterfaceInfo::getLeftNum, 0)
                .update();
    }

    /**
     * 回滚接口次数统计
     *
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackInvokeCount(long userId, long interfaceInfoId) {
        if (userId < 0 || interfaceInfoId < 0) {
            log.error("调用接口不存在");
            return false;
        }
        boolean update = lambdaUpdate()
                .setSql("totalNum = totalNum - 1 ,leftNum = leftNum +1")
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .gt(UserInterfaceInfo::getLeftNum, 0)
                .update();
        if(!update){
            log.error("接口回滚数据库操作失败");
            return false;
        }
        return true;
    }

    /**
     * 用户接口信息
     * @param searchValue
     * @param userId
     * @return
     */
    @Override
    public List<UserInterfaceInfoVo> getUserInterfaceInfoVoList(String searchValue, Long userId) {

        return userInterfaceInfoMapper.getUserInterfaceInfoVoList(searchValue,userId);
    }

}




