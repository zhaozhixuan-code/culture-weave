package com.zzx.cultureweavebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum StatementEnum {

    // 已授权
    ORIGINAL(1, "original"),
    // 未授权
    REPOST(2, "repost");

    private final Integer text;

    private final String value;

    StatementEnum(Integer text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static StatementEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (StatementEnum anEnum : StatementEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
