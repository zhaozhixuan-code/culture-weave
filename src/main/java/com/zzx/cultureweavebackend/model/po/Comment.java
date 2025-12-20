package com.zzx.cultureweavebackend.model.po;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import lombok.Data;

/**
 * 帖子评论表
 *
 * @TableName comment
 */
@TableName(value = "comment")
@Data
public class Comment {
    /**
     * 评论id（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的帖子id（关联post表id）
     */
    private Long postId;

    /**
     * 评论者用户id
     */
    private Long userId;

    /**
     * 父评论id：0=一级评论，>0=二级回复（关联自身id）
     */
    private Long parentId;

    /**
     * 被回复者用户id（parentId>0时必填）
     */
    private Long toUserId;

    /**
     * 评论内容（限制512字）
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0=正常，1=删除）
     */
    @TableLogic
    private Integer isDelete;
}