package com.cxb.backend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cxb.backend.mapper.InterfaceInfoMapper;
import com.cxb.backend.mapper.UserInterfaceInfoMapper;
import com.cxb.backend.model.vo.InvokeInterfaceInfoVO;
import com.cxb.backend.service.ChartService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.InterfaceInfo;
import com.cxb.apicommon.common.ErrorCode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author cxb
 */
@Service
public class ChartServiceImpl implements ChartService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public List<InvokeInterfaceInfoVO> listTopInvokeInterfaceInfo(int limit) {
        List<InvokeInterfaceInfoVO> vos = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(limit);
        if (vos == null || vos.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //根据id查询接口名称
        LinkedHashMap<Long, InvokeInterfaceInfoVO> map = new LinkedHashMap<>();
        vos.forEach(vo -> map.put(vo.getId(), vo));
        LambdaQueryWrapper<InterfaceInfo> lqw = new LambdaQueryWrapper<>();
        lqw.select(InterfaceInfo::getName, InterfaceInfo::getId);
        List<InterfaceInfo> interfaceInfos = interfaceInfoMapper.selectBatchIds(map.keySet());
        for (InterfaceInfo interfaceInfo : interfaceInfos) {
            map.get(interfaceInfo.getId()).setName(interfaceInfo.getName());
        }
        //public ArrayList(Collection<? extends E> c) 集合转list
        return new ArrayList<>(map.values()) ;
    }
}
