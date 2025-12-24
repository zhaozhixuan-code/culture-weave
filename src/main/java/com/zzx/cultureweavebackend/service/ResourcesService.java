package com.zzx.cultureweavebackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesAddRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesQueryRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesUpdateRequest;
import com.zzx.cultureweavebackend.model.po.Resources;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.ResourcesVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

/**
 * @author 28299
 * @description 针对表【resource(非遗资源)】的数据库操作Service
 * @createDate 2025-11-03 20:42:48
 */
public interface ResourcesService extends IService<Resources> {


    /**
     * 添加资源
     *
     * @param resourcesAddRequest
     * @param image
     * @param loginUser
     * @return
     */
    ResourcesVO addResources(ResourcesAddRequest resourcesAddRequest, MultipartFile image, User loginUser);

    /**
     * 删除资源
     *
     * @param id
     * @param request
     * @return
     */
    Boolean deleteResources(Long id, HttpServletRequest request);


    /**
     * 修改资源
     *
     * @param resourcesUpdateRequest
     * @param request
     * @return
     */
    Boolean updateResources(ResourcesUpdateRequest resourcesUpdateRequest, HttpServletRequest request);


    /**
     * 获取资源信息 （用户）
     *
     * @param id 资源id
     * @return 资源信息
     */
    ResourcesVO getResourcesVOById(Long id);


    /**
     * 获取查询条件
     *
     * @param resourcesQueryRequest
     * @return
     */
    Wrapper<Resources> getQueryWrapper(ResourcesQueryRequest resourcesQueryRequest);

    /**
     * 获取热门搜索词列表
     *
     * @return
     */
    Set<String> listHotSearchText();

    /**
     * 获取分页资源信息
     *
     * @param current
     * @param size
     * @param resourcesQueryRequest
     * @param loginUser
     * @return
     */
    Page<ResourcesVO> getResourcesVOPage(long current, long size, ResourcesQueryRequest resourcesQueryRequest, User loginUser);


    /**
     * 资源一键解释
     * @param resourcesId 资源id
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> explainResourcesByChat(Long resourcesId, User loginUser);

    /**
     * 获取用户搜索历史
     * @param userId 用户id
     * @return
     */
    List<String> getSearchHistoryByUserId(Long userId);
}
