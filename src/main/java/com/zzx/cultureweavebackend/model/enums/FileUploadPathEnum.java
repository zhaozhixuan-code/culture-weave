package com.zzx.cultureweavebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 文件上传路径枚举
 */
@Getter
public enum FileUploadPathEnum {


    RESOURCE("非遗资源", "resource"),
    PUBLIC("公共创作中心", "public"),
    AVATAR("用户头像", "avatar");

    private final String text;

    private final String value;

    FileUploadPathEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static FileUploadPathEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (FileUploadPathEnum anEnum : FileUploadPathEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
