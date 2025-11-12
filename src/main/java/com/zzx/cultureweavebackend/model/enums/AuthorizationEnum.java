package com.zzx.cultureweavebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AuthorizationEnum {

    // 已授权
    AUTHORIZED(1, "authorized"),
    // 未授权
    UNAUTHORIZED(2, "unauthorized");

    private final Integer text;

    private final String value;

    AuthorizationEnum(Integer text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AuthorizationEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AuthorizationEnum anEnum : AuthorizationEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
