package com.zzx.cultureweavebackend.model.dto.resources;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 修改资源请求
 */
@Data
public class ResourcesUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
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
