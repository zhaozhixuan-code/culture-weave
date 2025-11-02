package com.zzx.cultureweavebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.annotation.AuthCheck;
import com.zzx.cultureweavebackend.common.BaseResponse;
import com.zzx.cultureweavebackend.common.DeleteRequest;
import com.zzx.cultureweavebackend.common.ResultUtils;
import com.zzx.cultureweavebackend.exception.BusinessException;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.user.*;
import com.zzx.cultureweavebackend.model.enums.UserRoleEnum;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.LoginUserVO;
import com.zzx.cultureweavebackend.model.vo.UserVO;
import com.zzx.cultureweavebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    private UserService userService;


    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 添加用户（管理员）
     */
    @PostMapping("/add")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        Long userId = userService.addUser(userAddRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 删除用户（管理员）
     *
     * @param deleteRequest 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息（管理员）
     *
     * @param userUpdateRequest 更新参数
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(result);
    }

    /**
     * 编辑用户信息（用户）
     *
     * @param userEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUser(@RequestBody UserEditRequest userEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userEditRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!loginUser.getId().equals(userEditRequest.getId()), ErrorCode.NO_AUTH_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userEditRequest, user);
        String userPassword = user.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            String encryptionPassword = userService.getEncryptionPassword(userPassword);
            user.setUserPassword(encryptionPassword);
        }
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取用户（管理员）
     *
     * @param id 查询参数
     * @return 用户列表
     */
    @GetMapping("/get")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<User> getUserById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取用户脱敏信息（用户）
     *
     * @param id 查询参数
     * @return 用户列表
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam Long id) {
        User user = this.getUserById(id).getData();
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 获取用户列表（管理员）
     *
     * @param userQueryRequest 查询参数
     * @return 用户列表
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}