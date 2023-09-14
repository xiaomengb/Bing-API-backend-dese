package com.cxb.backend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cxb.backend.model.vo.UserKeyVo;
import com.cxb.backend.model.vo.UserVO;
import com.cxb.apicommon.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author yupi
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param captchaCode
     * @param request
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String captchaCode, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @param response
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @param response
     * @return
     */
    boolean userLogout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 生成图片验证码
     * @param request
     * @param response
     */
    void getCaptCha(HttpServletRequest request, HttpServletResponse response);


    boolean sendSmsCaptcha(String phoneNumber, String captchaType);

    UserVO userLoginByPhone(String phoneNumber, String captcha, HttpServletRequest request, HttpServletResponse response);

    UserKeyVo updateUserKey(Long userId);

    boolean updateUserInfo(String userPassword, String newUserPassword, String checkPassword, User user, HttpServletRequest request);

    boolean userUpdatePhone(String phoneNumber, String captcha, HttpServletRequest request);

    boolean userUpdatePhoneCheck(String phoneNumber, String captcha, HttpServletRequest request);
}
