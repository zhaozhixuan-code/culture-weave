package com.zzx.cultureweavebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.zzx.cultureweavebackend.model.dto.user.UserAddRequest;
import com.zzx.cultureweavebackend.model.dto.user.UserQueryRequest;
import com.zzx.cultureweavebackend.model.dto.user.UserRegisterRequest;
import com.zzx.cultureweavebackend.model.po.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.cultureweavebackend.model.vo.LoginUserVO;
import com.zzx.cultureweavebackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 28299
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-11-02 20:39:08
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptionPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    Long addUser(UserAddRequest userAddRequest);

    Wrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    boolean isAdmin(User user);
}
