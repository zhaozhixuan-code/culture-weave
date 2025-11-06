package com.zzx.cultureweavebackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 非遗资源分类
 */
@Data
public class ResourcesCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类列表
     */
    private List<String> categoryList;

    /**
     * 所有地区
     */
    private List<String> regionList;


}
