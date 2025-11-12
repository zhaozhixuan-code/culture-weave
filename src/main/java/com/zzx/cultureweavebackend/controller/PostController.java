package com.zzx.cultureweavebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.annotation.AuthCheck;
import com.zzx.cultureweavebackend.common.BaseResponse;
import com.zzx.cultureweavebackend.common.DeleteRequest;
import com.zzx.cultureweavebackend.common.ResultUtils;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.post.PostAddRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostQueryRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostUpdateRequest;
import com.zzx.cultureweavebackend.model.enums.UserRoleEnum;
import com.zzx.cultureweavebackend.model.po.Post;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.PostVO;
import com.zzx.cultureweavebackend.service.PostService;
import com.zzx.cultureweavebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 社区相关接口
 */
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;


    /**
     * 添加帖子
     *
     * @param image
     * @param postAddRequest
     * @param request
     * @return
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<PostVO> addPost(
            @RequestPart("file") MultipartFile image,
            @RequestPart("postAddRequest") @Valid PostAddRequest postAddRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        PostVO postVO = postService.addPost(postAddRequest, image, loginUser);
        return ResultUtils.success(postVO);
    }

    /**
     * 删除帖子
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        Boolean result = postService.deletePost(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 修改帖子
     *
     * @param postUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateResource(
            @RequestPart("file") MultipartFile image,
            @RequestPart("postUpdateRequest") @Valid PostUpdateRequest postUpdateRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Boolean result = postService.updatePost(postUpdateRequest, image, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取资源信息 （用户）
     *
     * @param id 资源id
     * @return 资源信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getResourcesVOById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        PostVO postVO = postService.getPostVOById(id);
        return ResultUtils.success(postVO);
    }

    /**
     * 获取帖子列表（管理员）
     *
     * @param
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 补充查询条件：根据创建时间倒序排序
        postQueryRequest.setSortField("createTime");
        Page<Post> pictureList = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(pictureList);
    }

    /**
     * 获取帖子列表（用户）
     *
     * @param
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listResourcesVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 分页查询
        Page<PostVO> postVOList = postService.getPostVOPage(current, size, postQueryRequest);
        return ResultUtils.success(postVOList);
    }
}
