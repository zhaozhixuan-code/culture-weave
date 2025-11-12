package com.zzx.cultureweavebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.model.dto.post.PostAddRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostQueryRequest;
import com.zzx.cultureweavebackend.model.dto.post.PostUpdateRequest;
import com.zzx.cultureweavebackend.model.po.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.PostVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 28299
 * @description 针对表【community】的数据库操作Service
 * @createDate 2025-11-10 20:48:05
 */
public interface PostService extends IService<Post> {

    PostVO addPost(PostAddRequest postAddRequest, MultipartFile image, User loginUser);

    Boolean deletePost(Long id, User loginUser);

    Boolean updatePost(PostUpdateRequest postUpdateRequest, MultipartFile image, User loginUser);

    PostVO getPostVOById(Long id);

    Page<PostVO> getPostVOPage(long current, long size, PostQueryRequest postQueryRequest);

    Wrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);
}
