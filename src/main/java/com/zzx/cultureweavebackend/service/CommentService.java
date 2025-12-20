package com.zzx.cultureweavebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.model.dto.comment.CommentAddRequest;
import com.zzx.cultureweavebackend.model.dto.comment.CommentQueryRequest;
import com.zzx.cultureweavebackend.model.po.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Zzx
 * @description 针对表【comment(帖子评论表)】的数据库操作Service
 * @createDate 2025-12-20 14:18:18
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     *
     * @param commentAddRequest
     * @param loginUser
     * @return
     */
    CommentVO addComment(CommentAddRequest commentAddRequest, User loginUser);

    /**
     * 删除评论
     *
     * @param id      评论id
     * @param request
     * @return
     */
    Boolean deleteResources(Long id, HttpServletRequest request);

    /**
     * 获取帖子评论列表
     *
     * @param postId
     * @return
     */
    Page<CommentVO> getCommentListByPostId(CommentQueryRequest postId);

}
