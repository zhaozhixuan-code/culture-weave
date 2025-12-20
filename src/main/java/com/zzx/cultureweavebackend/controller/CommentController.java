package com.zzx.cultureweavebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.annotation.AuthCheck;
import com.zzx.cultureweavebackend.common.BaseResponse;
import com.zzx.cultureweavebackend.common.DeleteRequest;
import com.zzx.cultureweavebackend.common.ResultUtils;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.comment.CommentAddRequest;
import com.zzx.cultureweavebackend.model.dto.comment.CommentQueryRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostQueryRequest;
import com.zzx.cultureweavebackend.model.enums.UserRoleEnum;
import com.zzx.cultureweavebackend.model.po.Comment;
import com.zzx.cultureweavebackend.model.po.Post;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.CommentVO;
import com.zzx.cultureweavebackend.service.CommentService;
import com.zzx.cultureweavebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论相关接口
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    /**
     * 添加评论
     *
     * @param commentAddRequest 添加评论请求参数
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<CommentVO> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(commentAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        CommentVO commentVO = commentService.addComment(commentAddRequest, loginUser);
        return ResultUtils.success(commentVO);
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 照片id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteResource(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Boolean result = commentService.deleteResources(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 获取评论列表
     *
     * @param commentQueryRequest 帖子id
     * @return 评论列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<CommentVO>> getCommentList(@RequestBody CommentQueryRequest commentQueryRequest) {
        ThrowUtils.throwIf(commentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<CommentVO> commentList = commentService.getCommentListByPostId(commentQueryRequest);
        return ResultUtils.success(commentList);
    }
}
