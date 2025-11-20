package com.zzx.cultureweavebackend.model.dto.resources;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 添加资源请求
 */
@Data
public class ResourcesAddRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资源id (用于修改)
     */
    private Long id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 地区
     */
    private String region;

    /**
     * 非遗传承人
     */
    private String userName;

    /**
     * 非遗传承人简介
     */
    private String userDescription;

    /**
     * 授权价格
     */
    private Integer price;
}
