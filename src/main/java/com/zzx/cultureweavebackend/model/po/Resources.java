package com.zzx.cultureweavebackend.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 非遗资源
 *
 * @TableName resource
 */
@TableName(value = "resources")
@Data
public class Resources {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 地区
     */
    private String region;

    /**
     * 资源图片
     */
    private String resourceImgUrl;

    /**
     * 授权价格
     */
    private Integer price;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建用户姓名
     */
    private String userName;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}