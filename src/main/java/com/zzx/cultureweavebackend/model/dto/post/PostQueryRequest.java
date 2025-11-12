package com.zzx.cultureweavebackend.model.dto.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.zzx.cultureweavebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostQueryRequest extends PageRequest implements Serializable {
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
     * 图片地址
     */
    private String pictureUrl;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

}
