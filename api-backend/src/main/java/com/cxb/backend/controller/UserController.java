package com.cxb.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import com.cxb.backend.annotation.AuthCheck;
import com.cxb.apicommon.common.BaseResponse;
import com.cxb.apicommon.common.DeleteRequest;
import com.cxb.apicommon.common.IdRequest;
import com.cxb.apicommon.common.ResultUtils;
import com.cxb.backend.model.dto.user.*;
import com.cxb.backend.model.vo.UserKeyVo;
import com.cxb.backend.model.vo.UserVO;
import com.cxb.backend.service.UserService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.common.ErrorCode;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @Value("${upload.path}")
    private String uploadPath;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        if (userRegisterRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String captchaCode = userRegisterRequest.getCaptchaCode();
        if (CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword, captchaCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, captchaCode, request);
        return ResultUtils.success(result);
    }

    /**
     * 用户账户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (CharSequenceUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userLogin(userAccount, userPassword, request, response);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户手机号登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/loginByPhone")
    public BaseResponse<UserVO> userLoginByPhone(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phoneNumber = userLoginRequest.getPhoneNumber();
        String captcha = userLoginRequest.getCaptcha();
        if (CharSequenceUtil.hasBlank(phoneNumber, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userLoginByPhone(phoneNumber, captcha, request, response);
        return ResultUtils.success(userVO);
    }

    /**
     * =图片验证码
     *
     * @param request
     * @param response
     */
    @GetMapping("/captcha")
    public void getCaptCha(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getCaptCha(request, response);

    }

    /**
     * 发送手机短信验证码
     *
     * @param smsCaptchaRequest
     */
    @GetMapping("/smsCaptcha")
    public BaseResponse<Boolean> sendSmsCaptcha(SmsCaptchaRequest smsCaptchaRequest) {
        if (smsCaptchaRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String captchaType = smsCaptchaRequest.getCaptchaType();
        String phoneNumber = smsCaptchaRequest.getPhoneNumber();
        if (CharSequenceUtil.hasBlank(captchaType, phoneNumber)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.sendSmsCaptcha(phoneNumber, captchaType);
        return ResultUtils.success(b);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request, response);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 封禁用户
     *
     * @param idRequest
     * @param request
     * @return
     */
    @GetMapping("/ban")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> banUser(IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(idRequest.getId());
        user.setStatus(1);
        boolean b = userService.updateById(user);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 解禁用户
     *
     * @param idRequest
     * @param request
     * @return
     */
    @GetMapping("/unban")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> unbanUser(IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(idRequest.getId());
        user.setStatus(0);
        boolean b = userService.updateById(user);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }


    /**
     * 更新用户信息
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update/info")
    public BaseResponse<Boolean> updateUserInfo(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPassword = userUpdateRequest.getUserPassword();
        String newUserPassword = userUpdateRequest.getNewUserPassword();
        String checkPassword = userUpdateRequest.getCheckPassword();
        User user = userService.getLoginUser(request);
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean b = userService.updateUserInfo(userPassword, newUserPassword, checkPassword, user, request);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取用户ak、sk
     *
     * @param request
     * @return
     */
    @GetMapping("/getKey")
    public BaseResponse<UserKeyVo> getUserKey(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = userService.getById(loginUser.getId());
        UserKeyVo userKeyVo = new UserKeyVo();
        userKeyVo.setAccessKey(user.getAccessKey());
        userKeyVo.setSecretKey(user.getSecretKey());

        return ResultUtils.success(userKeyVo);
    }

    /**
     * 重新生成ak、sk
     *
     * @param request
     * @return
     */
    @GetMapping("/updateKey")
    public BaseResponse<UserKeyVo> updateUserKey(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        UserKeyVo userKeyVo = userService.updateUserKey(loginUser.getId());
        return ResultUtils.success(userKeyVo);
    }

    /**
     * 更新头像
     *
     * @param request
     * @return
     */
    @PostMapping("/update/avatar")
    public BaseResponse<String> updateAvatar(MultipartFile file, HttpServletRequest request) {
        String newFileName = null;
        try {
            if (request == null || file == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User user = userService.getLoginUser(request);
            String originalFilename = file.getOriginalFilename();
            File oldFile = new File(uploadPath + user.getUserAvatar());
            //删除原文件
            if (oldFile.exists() && oldFile.isFile() && !user.getUserAvatar().equals("default.png")) {
                Files.delete(Paths.get(oldFile.getPath()));
            }
            //保存新头像
            if (originalFilename != null) {
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                newFileName = UUID.randomUUID() + suffix;
                file.transferTo(new File(uploadPath + newFileName));

            }
            user.setUserAvatar(newFileName);
            boolean b = userService.updateById(user);
            if (!b) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(newFileName);
    }

    /**
     * 用户修改手机号或绑定手机号
     *
     * @param userUpdatePhoneRequest
     * @param request
     * @return
     */
    @PostMapping("/update/phone")
    public BaseResponse<Boolean> userUpdatePhone(@RequestBody UserUpdatePhoneRequest userUpdatePhoneRequest, HttpServletRequest request) {
        if (userUpdatePhoneRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phoneNumber = userUpdatePhoneRequest.getPhoneNumber();
        String captcha = userUpdatePhoneRequest.getCaptcha();
        if (CharSequenceUtil.hasBlank(phoneNumber, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean b = userService.userUpdatePhone(phoneNumber, captcha, request);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }

    /**
     * 用户修改手机号校验
     *
     * @param userUpdatePhoneRequest
     * @param request
     * @return
     */
    @PostMapping("/check/update/phone")
    public BaseResponse<Boolean> userUpdatePhoneCheck(@RequestBody UserUpdatePhoneRequest userUpdatePhoneRequest, HttpServletRequest request) {
        if (userUpdatePhoneRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phoneNumber = userUpdatePhoneRequest.getPhoneNumber();
        String captcha = userUpdatePhoneRequest.getCaptcha();
        if (CharSequenceUtil.hasBlank(phoneNumber, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean b = userService.userUpdatePhoneCheck(phoneNumber, captcha, request);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(true);
    }


    /**
     * 下载sdk
     *
     * @param request
     */
    @GetMapping("/get/sdk")
    public void getSdk(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取要下载的文件
        ClassPathResource resource = new ClassPathResource("api-client-sdk-0.0.1.jar");
        InputStream inputStream = resource.getInputStream();

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=api-client-sdk-0.0.1.jar");

        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } finally {
            inputStream.close();
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    // endregion
}
