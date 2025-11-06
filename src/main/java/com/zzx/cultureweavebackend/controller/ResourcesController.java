package com.zzx.cultureweavebackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzx.cultureweavebackend.annotation.AuthCheck;
import com.zzx.cultureweavebackend.common.BaseResponse;
import com.zzx.cultureweavebackend.common.DeleteRequest;
import com.zzx.cultureweavebackend.common.ResultUtils;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesAddRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesQueryRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesUpdateRequest;
import com.zzx.cultureweavebackend.model.enums.UserRoleEnum;
import com.zzx.cultureweavebackend.model.po.Resources;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.ResourcesCategory;
import com.zzx.cultureweavebackend.model.vo.ResourcesVO;
import com.zzx.cultureweavebackend.service.ResourcesService;
import com.zzx.cultureweavebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 非遗资源相关接口
 */
@RestController
@RequestMapping("/resource")
public class ResourcesController {

    @Resource
    private UserService userService;

    @Resource
    private ResourcesService resourcesService;


    /**
     * 添加资源，包括图片上传，资源信息填写
     *
     * @param resourcesAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<ResourcesVO> addResource(@RequestPart("file") MultipartFile image,
                                                 ResourcesAddRequest resourcesAddRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ResourcesVO resourcesVO = resourcesService.addResources(resourcesAddRequest, image, loginUser);
        return ResultUtils.success(resourcesVO);
    }


    /**
     * 删除照片
     *
     * @param deleteRequest 照片id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteResource(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Boolean result = resourcesService.deleteResources(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }


    /**
     * 修改资源
     *
     * @param resourcesUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateResource(@RequestBody ResourcesUpdateRequest resourcesUpdateRequest, HttpServletRequest request) {
        Boolean result = resourcesService.updateResources(resourcesUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取资源信息 （用户）
     *
     * @param id 照片id
     * @return 照片信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<ResourcesVO> getPictureVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        ResourcesVO pictureVO = resourcesService.getResourcesVOById(id, request);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 获取资源列表（管理员）
     *
     * @param
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(value = UserRoleEnum.ADMIN)
    public BaseResponse<Page<Resources>> listPictureByPage(@RequestBody ResourcesQueryRequest resourcesQueryRequest) {
        ThrowUtils.throwIf(resourcesQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = resourcesQueryRequest.getCurrent();
        long size = resourcesQueryRequest.getPageSize();
        Page<Resources> pictureList = resourcesService.page(new Page<>(current, size), resourcesService.getQueryWrapper(resourcesQueryRequest));
        return ResultUtils.success(pictureList);
    }

    /**
     * 获取照片列表
     *
     * @param resourcesQueryRequest 非遗资源查询参数
     * @return 照片列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ResourcesVO>> listPictureVOByPage(@RequestBody ResourcesQueryRequest resourcesQueryRequest,
                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(resourcesQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = resourcesQueryRequest.getCurrent();
        long size = resourcesQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);

        // 分页查询
        Page<ResourcesVO> pictureVOList = resourcesService.getResourcesVOPage(current, size, resourcesQueryRequest, loginUser);
        return ResultUtils.success(pictureVOList);
    }


    /**
     * 获取预制的资源分类和地区
     *
     * @return
     */
    @GetMapping("category_list")
    public BaseResponse<ResourcesCategory> getResourceCategoryList() {
        ResourcesCategory resourcesCategory = new ResourcesCategory();
        List<String> categoryList = Arrays.asList("文学", "音乐", "舞蹈", "戏剧", "曲艺", "体育", "美术", "技艺", "医药", "民俗");
        List<String> regionList = Arrays.asList("安徽", "福建", "甘肃", "广东", "贵州", "海南", "河北", "河南", "黑龙江", "湖北", "湖南", "吉林", "江苏",
                "江西", "辽宁", "青海", "山东", "山西", "陕西", "四川", "云南", "浙江", "广西", "内蒙古",
                "宁夏", "西藏", "新疆", "北京", "重庆", "上海", "天津", "香港", "澳门", "台湾");
        resourcesCategory.setCategoryList(categoryList);
        resourcesCategory.setRegionList(regionList);
        return ResultUtils.success(resourcesCategory);
    }
}
