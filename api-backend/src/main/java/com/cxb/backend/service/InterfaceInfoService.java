package com.cxb.backend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cxb.apicommon.model.entity.InterfaceInfo;


/**
* @author 14105
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-09-07 17:44:06
*/

public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

}
