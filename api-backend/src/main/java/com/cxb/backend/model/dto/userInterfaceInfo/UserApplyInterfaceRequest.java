package com.cxb.backend.model.dto.userInterfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cxb
 */
@Data
public class UserApplyInterfaceRequest implements Serializable {

    /**
     * 调用用户Id
     */
    private Long userId;

    /**
     * 接口Id
     */
    private Long interfaceInfoId;

    /**
     * 申请次数
     */
    private Integer applyNum;


}
