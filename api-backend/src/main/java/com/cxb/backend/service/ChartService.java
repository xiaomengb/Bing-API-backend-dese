package com.cxb.backend.service;


import com.cxb.backend.model.vo.InvokeInterfaceInfoVO;

import java.util.List;

/**
 * @author cxb
 */
public interface ChartService {

    List<InvokeInterfaceInfoVO> listTopInvokeInterfaceInfo(int limit);
}
