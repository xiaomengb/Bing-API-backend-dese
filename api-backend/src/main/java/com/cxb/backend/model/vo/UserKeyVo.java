package com.cxb.backend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cxb
 */
@Data
public class UserKeyVo implements Serializable {

    /**
     * ak
     */
    private String accessKey;

    /**
     * sk
     */
    private String secretKey;
}
