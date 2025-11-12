package com.zzx.cultureweavebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.zzx.cultureweavebackend.model.po.Post;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class PostVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

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

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 授权状态：1-未授权，2-已授权
     */
    private Integer authorization;

    /**
     * 内容声明：1-原创，2-转载
     */
    private Integer statement;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 对象转包装类
     *
     * @param post
     * @return
     */
    public static PostVO objToVo(Post post) {
        if (post == null) {
            return null;
        }
        PostVO postVO = new PostVO();
        BeanUtil.copyProperties(post, postVO);
        return postVO;
    }

    /**
     * 包装类转对象
     *
     * @param postVO
     * @return
     */
    public static Post voToObj(PostVO postVO) {
        if (postVO == null) {
            return null;
        }
        Post post = new Post();
        BeanUtil.copyProperties(postVO, post);
        return post;
    }

}
