package com.cxb.backend.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author cxb
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * 0-正常 ，1-禁用
     */
    private Integer status;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 新密码
     */
    private String newUserPassword;

    /**
     * 确认
     */
    private String checkPassword;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}