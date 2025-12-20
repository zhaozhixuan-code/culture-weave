package com.zzx.cultureweavebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.comment.CommentAddRequest;
import com.zzx.cultureweavebackend.model.dto.comment.CommentQueryRequest;
import com.zzx.cultureweavebackend.model.po.Comment;
import com.zzx.cultureweavebackend.model.po.Post;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.CommentVO;
import com.zzx.cultureweavebackend.model.vo.UserVO;
import com.zzx.cultureweavebackend.service.CommentService;
import com.zzx.cultureweavebackend.mapper.CommentMapper;
import com.zzx.cultureweavebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【comment(帖子评论表)】的数据库操作Service实现
 * @createDate 2025-12-20 14:18:18
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    /**
     * 添加评论
     *
     * @param commentAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public CommentVO addComment(CommentAddRequest commentAddRequest, User loginUser) {
        Long postId = commentAddRequest.getPostId();
        ThrowUtils.throwIf(postId == null, ErrorCode.PARAMS_ERROR);
        String content = commentAddRequest.getContent();
        ThrowUtils.throwIf(content == null, ErrorCode.PARAMS_ERROR);
        Long userId = loginUser.getId();
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        // 保存评论
        boolean save = save(comment);
        // 获取创建者信息
        UserVO creator = userService.getUserVO(loginUser);
        CommentVO commentVO = new CommentVO();
        BeanUtil.copyProperties(comment, commentVO);
        commentVO.setCreator(creator);
        return commentVO;
    }

    /**
     * 删除评论
     *
     * @param id      评论 id
     * @param request
     * @return
     */
    @Override
    public Boolean deleteResources(Long id, HttpServletRequest request) {
        Comment comment = getById(id);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        // 获得创建者 id
        Long userId = comment.getUserId();
        User loginUser = userService.getLoginUser(request);
        // 仅管理员或创建者可以删除
        ThrowUtils.throwIf(!userId.equals(loginUser.getId()) || !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        boolean result = removeById(id);
        return result;
    }

    /**
     * 根据帖子 id 获取评论列表
     *
     * @param commentQueryRequest
     * @return
     */
    @Override
    public Page<CommentVO> getCommentListByPostId(CommentQueryRequest commentQueryRequest) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 补充查询条件：根据创建时间倒序排序
        commentQueryRequest.setSortField("createTime");
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        // 构造查询条件
        queryWrapper.eq(commentQueryRequest.getPostId() != null, "postId", commentQueryRequest.getPostId());
        // 分页查询
        Page<Comment> commentPage = page(new Page<>(current, size), queryWrapper);
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if(CollUtil.isEmpty(commentList)){
            return commentVOPage;
        }
        // 获取返回包装类
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtil.copyProperties(comment, commentVO);
            return commentVO;
        }).toList();
        // 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userNameMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 为每一个帖子信息，设置用户信息
        commentVOList.forEach(commentVO -> {
            Long userId = commentVO.getUserId();
            User user = userNameMap.get(userId).get(0);
            commentVO.setCreator(userService.getUserVO(user));
        });
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }


}




