package com.zzx.cultureweavebackend.model.dto.post;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PostAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id (用于修改)
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 商业创作授权状态：0-非商业 1-未授权，2-已授权
     */
    private Integer authorization;

    /**
     * 内容声明：1-原创，2-转载
     */
    private Integer statement;

}