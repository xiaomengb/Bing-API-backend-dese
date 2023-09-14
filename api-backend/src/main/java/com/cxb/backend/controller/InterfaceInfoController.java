package com.cxb.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxb.apiclientsdk.client.BingApiClient;

import com.cxb.apiclientsdk.common.ApiResult;
import com.cxb.apicommon.common.BaseResponse;
import com.cxb.apicommon.common.DeleteRequest;
import com.cxb.apicommon.common.IdRequest;
import com.cxb.apicommon.common.ResultUtils;
import com.cxb.backend.annotation.AuthCheck;

import com.cxb.apicommon.constant.CommonConstant;
import com.cxb.backend.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.cxb.backend.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.cxb.backend.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.cxb.backend.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.cxb.backend.model.enums.InterfaceInfoStatusEnum;
import com.cxb.backend.model.vo.InterfaceInfoVo;
import com.cxb.backend.model.vo.RequestParamsRemarkVO;
import com.cxb.backend.model.vo.ResponseParamsRemarkVO;
import com.cxb.backend.service.InterfaceInfoService;
import com.cxb.backend.service.UserInterfaceInfoService;
import com.cxb.backend.service.UserService;
import com.cxb.apicommon.exception.BusinessException;
import com.cxb.apicommon.model.entity.InterfaceInfo;
import com.cxb.apicommon.model.entity.User;
import com.cxb.apicommon.model.entity.UserInterfaceInfo;
import com.cxb.apicommon.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * api接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private BingApiClient bingApiClient;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        //请求参数和响应参数说明
        List<RequestParamsRemarkVO> requestParamsRemark = interfaceInfoAddRequest.getRequestParamsRemark();
        interfaceInfo.setRequestParams(JSONUtil.toJsonStr(requestParamsRemark));
        List<ResponseParamsRemarkVO> responseParamsRemark = interfaceInfoAddRequest.getResponseParamsRemark();
        interfaceInfo.setResponseParams(JSONUtil.toJsonStr(responseParamsRemark));
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        //请求参数和响应参数说明
        List<RequestParamsRemarkVO> requestParamsRemark = interfaceInfoUpdateRequest.getRequestParamsRemark();
        interfaceInfo.setRequestParams(JSONUtil.toJsonStr(requestParamsRemark));
        List<ResponseParamsRemarkVO> responseParamsRemark = interfaceInfoUpdateRequest.getResponseParamsRemark();
        interfaceInfo.setResponseParams(JSONUtil.toJsonStr(responseParamsRemark));
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 发布
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                     HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User user = userService.getLoginUser(request);
        //判断接口是否可以调用
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String userRequestParams = interfaceInfo.getParameterExample();
        Map<String, Object> paramMap = JSONUtil.parseObj(userRequestParams);
        BingApiClient apiClient = new BingApiClient(user.getAccessKey(), user.getSecretKey());
        ApiResult apiResult = apiClient.invoke(url, method, paramMap);

        if (apiResult.getCode() != 0) {
            throw new BusinessException(apiResult.getCode(), apiResult.getMessage());
        }

        InterfaceInfo intf = new InterfaceInfo();
        intf.setId(id);
        intf.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean b = interfaceInfoService.updateById(intf);
        return ResultUtils.success(b);
    }

    /**
     * 下线
     *
     * @param idRequest idRequest
     * @param request request
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest,
                                                      HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        /*//判断接口是否可以调用
        ApiResult result = apiClient.getUsername(new com.cxb.apiclientsdk.model.User("cxb"));
        if (result.getCode() != 0) {
            throw new BusinessException(result.getCode(), result.getMessage());

        }*/
        InterfaceInfo intf = new InterfaceInfo();
        intf.setId(id);
        intf.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean b = interfaceInfoService.updateById(intf);
        return ResultUtils.success(b);
    }

    /**
     * 调用
     *
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<ApiResult> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                                            HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1.判断是否存在
        long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2.判断用户是否有调用次数
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        if (!loginUser.getUserRole().equals("admin")) {
            UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                    .eq(UserInterfaceInfo::getUserId, userId)
                    .eq(UserInterfaceInfo::getInterfaceInfoId, id)
                    .gt(UserInterfaceInfo::getLeftNum, 0)
                    .one();
            if (userInterfaceInfo == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用次数不足！");
            }
        }

        //3.接口调用

        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        Map<String, Object> paramMap = JSONUtil.parseObj(userRequestParams);
        BingApiClient apiClient = new BingApiClient(accessKey, secretKey);
        ApiResult apiResult = apiClient.invoke(url, method, paramMap);

        return ResultUtils.success(apiResult);

    }


    /**
     * 根据 id 获取接口详情
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfoVo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        InterfaceInfoVo interfaceInfoVo = BeanUtil.copyProperties(interfaceInfo, InterfaceInfoVo.class);
        //写入请求参数和备注
        interfaceInfoVo.setRequestParamsRemark(
                JSONUtil.toList(interfaceInfo.getRequestParams(), RequestParamsRemarkVO.class)
        );
        //写入响应参数和备注
        interfaceInfoVo.setResponseParamsRemark(
                JSONUtil.toList(interfaceInfo.getResponseParams(), ResponseParamsRemarkVO.class)
        );
        return ResultUtils.success(interfaceInfoVo);
    }


    /**
     * 分页获取接口列表(仅管理员可使用)
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<InterfaceInfoVo>> listInterfaceInfoVoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String name = interfaceInfoQueryRequest.getName();
        String method = interfaceInfoQueryRequest.getMethod();
        String url = interfaceInfoQueryRequest.getUrl();
        Integer status = interfaceInfoQueryRequest.getStatus();

        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<InterfaceInfo> page = interfaceInfoService.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(name), InterfaceInfo::getName, name)
                .or()
                .like(CharSequenceUtil.isNotBlank(method), InterfaceInfo::getMethod, name)
                .or()
                .like(CharSequenceUtil.isNotBlank(url), InterfaceInfo::getUrl, url)
                .or()
                .like(status != null, InterfaceInfo::getStatus, status)
                .page(new Page<>(current, size));
        //写入响应参数和备注、请求参数和备注
        Page<InterfaceInfoVo> interfaceInfoVoPage = new Page<>(size, current, page.getTotal());
        interfaceInfoVoPage.setRecords(page.getRecords().stream().map(info -> {
            InterfaceInfoVo interfaceInfoVo = BeanUtil.copyProperties(info, InterfaceInfoVo.class);
            interfaceInfoVo.setRequestParamsRemark(
                    JSONUtil.toList(info.getRequestParams(), RequestParamsRemarkVO.class)
            );
            interfaceInfoVo.setResponseParamsRemark(
                    JSONUtil.toList(info.getResponseParams(), ResponseParamsRemarkVO.class)
            );
            return interfaceInfoVo;
        }).collect(Collectors.toList()));

        return ResultUtils.success(interfaceInfoVoPage);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取商城接口列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/store/list/page")
    public BaseResponse<Page<InterfaceInfo>> listStoreInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        String searchValue = interfaceInfoQueryRequest.getSearchValue();
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();

        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<InterfaceInfo> page = interfaceInfoService.lambdaQuery()
                .like(CharSequenceUtil.isNotBlank(searchValue), InterfaceInfo::getName, searchValue)
                .or()
                .like(CharSequenceUtil.isNotBlank(searchValue), InterfaceInfo::getDescription, searchValue)
                .eq(InterfaceInfo::getStatus, 1)
                .page(new Page<>(current, size));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(CharSequenceUtil.isNotBlank(description), "description", description);
        queryWrapper.orderBy(CharSequenceUtil.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

    // endregion

}
