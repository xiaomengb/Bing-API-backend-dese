package com.cxb.apicommon.service;


import com.cxb.apicommon.model.entity.User;


/**
 * 用户远程调用服务
 *
 * @author cxb
 */
public interface InnerUserService {

    /**
     * 根据accessKey查询用户
     *
     * @param accessKey accessKey
     * @return User
     */
    User getInvokeUser(String accessKey);


    /**
     * 查询用户
     * @param userId
     * @return
     */
    User getLoginUser(Long userId);
}
