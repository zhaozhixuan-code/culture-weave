package com.zzx.cultureweavebackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帖子评论视图
 */
@Data
public class CommentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论 id
     */
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 创建者 id
     */
    private Long userId;

    /**
     * 创建者
     */
    private UserVO creator;
}
