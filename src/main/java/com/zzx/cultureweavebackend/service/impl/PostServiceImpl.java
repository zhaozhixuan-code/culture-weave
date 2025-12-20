package com.zzx.cultureweavebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.cultureweavebackend.exception.BusinessException;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.post.PostAddRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostQueryRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostUpdateRequest;
import com.zzx.cultureweavebackend.model.enums.FileUploadPathEnum;
import com.zzx.cultureweavebackend.model.po.Post;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.PostVO;
import com.zzx.cultureweavebackend.model.vo.UploadPictureResult;
import com.zzx.cultureweavebackend.model.vo.UserVO;
import com.zzx.cultureweavebackend.service.PostService;
import com.zzx.cultureweavebackend.mapper.PostMapper;
import com.zzx.cultureweavebackend.service.UserService;
import com.zzx.cultureweavebackend.utils.upload.FilePictureUpload;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 28299
 * @description 针对表【community】的数据库操作Service实现
 * @createDate 2025-11-10 20:48:05
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {



    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;

    /**
     * 添加帖子
     *
     * @param postAddRequest 帖子信息
     * @param image          图片
     * @param loginUser      登录用户
     * @return 帖子信息
     */
    @Override
    public PostVO addPost(PostAddRequest postAddRequest, MultipartFile image, User loginUser) {
        // 判断是修改资源还是新增资源
        Long postId;

        if (postAddRequest != null) {
            postId = postAddRequest.getId();
        } else {
            postId = null;
        }
        // 如果是更新资源，需要校验资源是否存在
        if (postId != null) {
            Post post = this.getById(postId);
            ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        }
        Post post = new Post();
        // 上传图片
        String path = String.format(FileUploadPathEnum.PUBLIC + "/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = filePictureUpload.uploadPicture(image, path);
        // 获取图片访问地址
        String imagePath = uploadPictureResult.getUrl();
        // 存入数据库
        post.setPictureUrl(imagePath);
        post.setTitle(postAddRequest.getTitle());
        post.setContent(postAddRequest.getContent());
        post.setUserId(loginUser.getId());
        if(postAddRequest.getAuthorization() == null){
            // 没有填写授权状态，默认非商业为0
            post.setAuthorization(0);
        }else{
            post.setAuthorization(postAddRequest.getAuthorization());
        }
        post.setStatement(postAddRequest.getStatement());
        boolean result;
        if (postId != null) {
            post.setId(postId);
            post.setEditTime(new Date());
            result = this.update().eq("id", postId)
                    .update(post);
        } else {
            result = this.save(post);
        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        PostVO postVO = PostVO.objToVo(post);
        postVO.setUserVO(userService.getUserVO(loginUser));
        return postVO;
    }

    /**
     * 删除帖子
     *
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    public Boolean deletePost(Long id, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Post post = this.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);

        checkResources(post, loginUser);
        boolean result = this.removeById(id);
        return result;
    }

    /**
     * 修改帖子
     *
     * @param postUpdateRequest
     * @param image
     * @param loginUser
     * @return
     */
    @Override
    public Boolean updatePost(PostUpdateRequest postUpdateRequest, MultipartFile image, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(postUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Post post = this.getById(postUpdateRequest.getId());
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        checkResources(post, loginUser);
        Post updatePost = new Post();
        BeanUtil.copyProperties(postUpdateRequest, updatePost);
        // 上传图片
        if (image != null) {
            // 上传图片
            String path = String.format(FileUploadPathEnum.PUBLIC + "/%s", loginUser.getId());
            UploadPictureResult uploadPictureResult = filePictureUpload.uploadPicture(image, path);
            // 获取图片访问地址
            String imagePath = uploadPictureResult.getUrl();
            updatePost.setPictureUrl(imagePath);
        }

        boolean result = this.update().eq("id", postUpdateRequest.getId())
                .update(updatePost);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 获取帖子信息（用户）
     *
     * @param id
     * @return
     */
    @Override
    public PostVO getPostVOById(Long id) {
        // 校验参数
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Post post = this.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取返回包装类
        PostVO postVO = PostVO.objToVo(post);
        Long postUserId = post.getUserId();
        User user = userService.getById(postUserId);
        UserVO postUserVO = userService.getUserVO(user);
        // 设置帖子用户信息
        postVO.setUserVO(postUserVO);
        return postVO;
    }


    /**
     * 获取分页（VO）资源信息
     *
     * @param current
     * @param size
     * @param postQueryRequest
     * @return
     */
    @Override
    public Page<PostVO> getPostVOPage(long current, long size, PostQueryRequest postQueryRequest) {
        // 获取分页资源信息
        Page<Post> postPage = this.page(new Page<>(current, size), this.getQueryWrapper(postQueryRequest));
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 获取返回包装类
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtil.copyProperties(post, postVO);
            return postVO;
        }).toList();
        // 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userNameMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 为每一个资源信息，设置用户信息
        postVOList.forEach(postVO -> {
            Long userId = postVO.getUserId();
            if (userNameMap.containsKey(userId)) {
                User user = userNameMap.get(userId).get(0);
                postVO.setUserVO(userService.getUserVO(user));
            }
        });
        postVOPage.setRecords(postVOList);

        return postVOPage;
    }


    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public Wrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        // 获取所有参数
        Long id = postQueryRequest.getId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText)
                    .or()
                    .like("content", searchText)
            );
        }
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }

    /**
     * 用于校验权限，只有管理员或者本人可以操作
     *
     * @param post
     * @param loginUser
     */
    public void checkResources(Post post, User loginUser) {
        Long userId = loginUser.getId();
        if (!userId.equals(post.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
