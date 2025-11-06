package com.zzx.cultureweavebackend.model.dto.resources;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.zzx.cultureweavebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ResourcesQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 搜索词
     */
    private String searchText;

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
     * 创建用户姓名
     */
    private String userName;

}
