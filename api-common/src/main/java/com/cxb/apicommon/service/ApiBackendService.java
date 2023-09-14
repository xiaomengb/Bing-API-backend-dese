package com.cxb.apicommon.service;

import com.cxb.apicommon.model.entity.InterfaceInfo;
import org.springframework.transaction.annotation.Transactional;

/**
 * api接口管理系统远程调用服务
 *
 * @author cxb
 */
public interface ApiBackendService {

    /**
     * 根据path、method查询接口信息
     *
     * @param url   请求路径
     * @param method 请求方法
     * @return InterfaceInfo
     */
    InterfaceInfo getInvokeInterfaceInfo(String url, String method);

    /**
     * 是否还有调用次数
     *
     * @param userId          用户id
     * @param interfaceInfoId 接口id
     * @return boolean
     */
    boolean hasInvokeNum(long userId, long interfaceInfoId);


    /**
     * 根据userId、interfaceInfoId计数
     *
     * @param userId          用户id
     * @param interfaceInfoId 接口id
     * @return boolean
     */
    Boolean[] invokeInterfaceCount(long userId, long interfaceInfoId);

    InterfaceInfo getInterfaceInfoById(Long interfaceId);

    int getPayInterfaceStock(Long interfaceId);

    @Transactional(rollbackFor = Exception.class)
    boolean updatePayInterfaceStock(Long interfaceId, int num);

    @Transactional(rollbackFor = Exception.class)
    boolean rollbackPayInterfaceStock(Long interfaceId, Integer count);

    boolean updateUserInterfaceInvokeCount(Long userId, Long interfaceId, Integer count);
}
