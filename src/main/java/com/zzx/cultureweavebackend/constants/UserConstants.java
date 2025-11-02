package com.zzx.cultureweavebackend.constants;

/**
 * 用户常量
 */
public interface UserConstants {

    /**
     * 加密密钥
     */
    String ENCRYPTION_KEY = "cultureweavebackend";


    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";


    String DEFAULT_PASSWORD = "12345678";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    // endregion


}
