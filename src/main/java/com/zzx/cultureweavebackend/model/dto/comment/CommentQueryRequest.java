package com.zzx.cultureweavebackend.model.dto.comment;

import com.zzx.cultureweavebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 帖子id
     */
    private Long postId;
}

