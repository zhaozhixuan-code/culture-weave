package com.zzx.cultureweavebackend.model.dto.post;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PostUpdateRequest implements Serializable {

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
     * 图片路径
     */
    private String imageUrl;

    /**
     * 授权状态：1-未授权，2-已授权
     */
    private Integer authorization;

    /**
     * 内容声明：1-原创，2-转载
     */
    private Integer statement;

}