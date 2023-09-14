package com.cxb.backend.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cxb.backend.annotation.AuthCheck;
import com.cxb.apicommon.common.BaseResponse;
import com.cxb.apicommon.common.DeleteRequest;
import com.cxb.apicommon.common.ResultUtils;
import com.cxb.apicommon.constant.CommonConstant;
import com.cxb.backend.constant.UserConstant;
import com.cxb.backend.model.dto.userInterfaceInfo.UserApplyInterfaceRequest;
import com.cxb.backend.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.cxb.backend.model.dto.userInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.cxb.backend.model.dto.userInterfaceInfo.UserInterfaceInfoUpdateRequest;
import com.cxb.backend.model.vo.UserInterfaceInfoVo;
import com.cxb.backend.service.UserInterfaceInfoService;
import com.cxb.backend.service.UserService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * api接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;


    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        // 校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param userInterfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        return ResultUtils.success(userInterfaceInfo);
    }

//    /**
//     * 获取列表（仅管理员可使用）
//     *
//     * @param userInterfaceInfoQueryRequest
//     * @return
//     */
//    @GetMapping("/list")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<List<UserInterfaceInfo>> listUserInterfaceInfo(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
//        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
//        if (userInterfaceInfoQueryRequest != null) {
//            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
//        }
//        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
//        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
//        return ResultUtils.success(userInterfaceInfoList);
//    }

    /**
     * 获取用户接口列表
     *
     * @param userInterfaceInfoQueryRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfoVo>> getUserInterfaceInfoVoList(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String searchValue = userInterfaceInfoQueryRequest.getSearchValue();
        User loginUser = userService.getLoginUser(request);
        List<UserInterfaceInfoVo> userInterfaceInfoVoList = userInterfaceInfoService.getUserInterfaceInfoVoList(searchValue, loginUser.getId());
        return ResultUtils.success(userInterfaceInfoVoList);
    }

    /**
     * 分页获取列表
     *
     * @param userInterfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        queryWrapper.orderBy(CharSequenceUtil.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(userInterfaceInfoPage);
    }


    /**
     * 用户申请免费接口调用次数
     *
     * @param userApplyInterfaceRequest
     * @param request
     * @return
     */
    @PostMapping("/apply/interface")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Integer> userApplyInterface(UserApplyInterfaceRequest userApplyInterfaceRequest, HttpServletRequest request) {
        if (userApplyInterfaceRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long interfaceInfoId = userApplyInterfaceRequest.getInterfaceInfoId();
        User loginUser = userService.getLoginUser(request);
        if (interfaceInfoId == null || loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, loginUser.getId())
                .one();
        int applyNum = 100;
        if (userInterfaceInfo == null) {
            UserInterfaceInfo save = new UserInterfaceInfo();
            save.setInterfaceInfoId(interfaceInfoId);
            save.setUserId(loginUser.getId());
            userInterfaceInfoService.save(save);
            save.setLeftNum(applyNum);
            boolean b = userInterfaceInfoService.updateById(save);
            if (!b) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return ResultUtils.success(applyNum);
        }
        if (userInterfaceInfo.getLeftNum() == 500) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此接口调用次数已达上线！");
        }
        if (userInterfaceInfo.getLeftNum() + applyNum > 500) {
            applyNum = 500 - userInterfaceInfo.getLeftNum();
        }
        userInterfaceInfo.setLeftNum(applyNum + userInterfaceInfo.getLeftNum());
        boolean b = userInterfaceInfoService.updateById(userInterfaceInfo);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(applyNum);
    }
    // endregion

}
