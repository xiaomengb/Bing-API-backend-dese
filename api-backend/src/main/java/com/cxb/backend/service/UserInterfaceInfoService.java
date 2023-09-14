package com.cxb.backend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cxb.backend.model.vo.UserInterfaceInfoVo;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author 14105
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Service
* @createDate 2023-08-10 12:12:26
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);


    boolean invokeCount(long userId,long interfaceInfoId);

    boolean rollbackInvokeCount(long userId,long interfaceInfoId);

    List<UserInterfaceInfoVo> getUserInterfaceInfoVoList(String searchValue, Long userId);
}
