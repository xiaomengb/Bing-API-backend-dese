package com.cxb.backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxb.backend.model.vo.InvokeInterfaceInfoVO;
import com.cxb.backend.model.vo.UserInterfaceInfoVo;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 14105
* @description 针对表【user_interface_info(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2023-08-10 12:12:26
* @Entity com.cxb.apicommon.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<InvokeInterfaceInfoVO> listTopInvokeInterfaceInfo(int limit);

    List<UserInterfaceInfoVo> getUserInterfaceInfoVoList(@Param("searchValue") String searchValue, @Param("userId") Long userId);
}




