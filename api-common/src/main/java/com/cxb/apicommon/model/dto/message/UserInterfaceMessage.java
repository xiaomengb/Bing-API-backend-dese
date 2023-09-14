package com.cxb.apicommon.model.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author cxb
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInterfaceMessage implements Serializable {

    private Long userId;

    private Long interfaceId;


}
