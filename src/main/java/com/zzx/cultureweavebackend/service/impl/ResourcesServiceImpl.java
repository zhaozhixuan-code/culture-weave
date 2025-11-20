package com.zzx.cultureweavebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.cultureweavebackend.ai.ResourcesChatApp;
import com.zzx.cultureweavebackend.exception.BusinessException;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesAddRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesQueryRequest;
import com.zzx.cultureweavebackend.model.dto.resources.ResourcesUpdateRequest;
import com.zzx.cultureweavebackend.model.enums.FileUploadPathEnum;
import com.zzx.cultureweavebackend.model.po.Resources;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.model.vo.ResourcesVO;
import com.zzx.cultureweavebackend.model.vo.UploadPictureResult;
import com.zzx.cultureweavebackend.service.ResourcesService;
import com.zzx.cultureweavebackend.mapper.ResourcesMapper;
import com.zzx.cultureweavebackend.service.UserService;
import com.zzx.cultureweavebackend.utils.upload.FilePictureUpload;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 28299
 * @description 针对表【resource(非遗资源)】的数据库操作Service实现
 * @createDate 2025-11-03 20:42:48
 */
@Service
public class ResourcesServiceImpl extends ServiceImpl<ResourcesMapper, Resources>
        implements ResourcesService {



    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ResourcesChatApp resourcesChatApp;


    /**
     * 添加资源
     *
     * @param resourcesAddRequest
     * @param image
     * @param loginUser
     * @return
     */
    @Override
    public ResourcesVO addResources(ResourcesAddRequest resourcesAddRequest, MultipartFile image, User loginUser) {
        // 判断是修改资源还是新增资源
        Long resourcesId;
        if (resourcesAddRequest != null) {
            resourcesId = resourcesAddRequest.getId();
        } else {
            resourcesId = null;
        }
        // 如果是更新资源，需要校验资源是否存在
        if (resourcesId != null) {
            Resources resources = this.getById(resourcesId);
            ThrowUtils.throwIf(resources == null, ErrorCode.NOT_FOUND_ERROR);
        }
        // 上传图片
        String path = String.format(FileUploadPathEnum.RESOURCE.getValue() + "/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = filePictureUpload.uploadPicture(image, path);
        // 获取图片访问地址
        String imagePath = uploadPictureResult.getUrl();
        // 存入数据库
        Resources resources = new Resources();
        resources.setName(resourcesAddRequest.getName());
        resources.setIntroduction(resourcesAddRequest.getIntroduction());
        resources.setCategory(resourcesAddRequest.getCategory());
        List<String> tags = resourcesAddRequest.getTags();
        resources.setTags(JSONUtil.toJsonStr(tags));
        resources.setRegion(resourcesAddRequest.getRegion());
        resources.setResourceImgUrl(imagePath);
        resources.setUserId(loginUser.getId());
        resources.setUserName(resources.getUserName());
        resources.setPrice(resourcesAddRequest.getPrice());
        resources.setUserDescription(resourcesAddRequest.getUserDescription());
        // 如果不等于null，则更新
        boolean result;
        if (resourcesId != null) {
            resources.setId(resourcesId);
            resources.setEditTime(new Date());
            result = this.update().eq("id", resourcesId)
                    .update(resources);
        } else {
            result = this.save(resources);
        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        ResourcesVO resourcesVO = new ResourcesVO();
        BeanUtil.copyProperties(resources, resourcesVO);
        resourcesVO.setTags(tags);
        return resourcesVO;
    }

    /**
     * 删除资源
     *
     * @param id      资源id
     * @param request
     * @return
     */
    @Override
    public Boolean deleteResources(Long id, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Resources resources = this.getById(id);
        ThrowUtils.throwIf(resources == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取用户id，只有本人或者管理员才能删
        User loginUser = userService.getLoginUser(request);
        checkResources(resources, loginUser);
        boolean result = this.removeById(id);
        return result;
    }

    /**
     * 修改资源
     *
     * @param resourcesUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateResources(ResourcesUpdateRequest resourcesUpdateRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(resourcesUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验资源是否存在
        Resources resources = this.getById(resourcesUpdateRequest.getId());
        ThrowUtils.throwIf(resources == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        User loginUser = userService.getLoginUser(request);
        checkResources(resources, loginUser);
        // 补充参数
        BeanUtil.copyProperties(resourcesUpdateRequest, resources);
        resources.setTags(JSONUtil.toJsonStr(resourcesUpdateRequest.getTags()));
        resources.setEditTime(new Date());
        // 修改资源
        boolean result = this.update().eq("id", resources.getId())
                .update(resources);
        return result;
    }

    /**
     * 获取资源信息 （用户）
     *
     * @param id 资源id
     * @return 资源信息
     */
    @Override
    public ResourcesVO getResourcesVOById(Long id) {
        // 校验参数
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        // 获取资源信息
        Resources resources = this.getById(id);
        ThrowUtils.throwIf(resources == null, ErrorCode.NOT_FOUND_ERROR);
        ResourcesVO resourcesVO = new ResourcesVO();
        BeanUtil.copyProperties(resources, resourcesVO);
        resourcesVO.setTags(JSONUtil.parseArray(resources.getTags()).toList(String.class));
        return resourcesVO;
    }

    /**
     * 获取分页资源信息
     *
     * @param current
     * @param size
     * @param resourcesQueryRequest
     * @return
     */
    @Override
    public Page<ResourcesVO> getResourcesVOPage(long current, long size, ResourcesQueryRequest resourcesQueryRequest) {
        // 获取分页资源信息
        // 根据创建时间排序查询
        resourcesQueryRequest.setSortField("createTime");
        Page<Resources> resourcesPage = this.page(new Page<>(current, size), this.getQueryWrapper(resourcesQueryRequest));
        List<Resources> resourcesList = resourcesPage.getRecords();
        Page<ResourcesVO> resourcesVOPage = new Page<>(resourcesPage.getCurrent(), resourcesPage.getSize(), resourcesPage.getTotal());
        if (CollUtil.isEmpty(resourcesList)) {
            return resourcesVOPage;
        }
        List<ResourcesVO> resourcesVOList = resourcesList.stream().map(resources -> {
            ResourcesVO resourcesVO = new ResourcesVO();
            BeanUtil.copyProperties(resources, resourcesVO);
            // 把JSON格式的标签转换成List<String>
            resourcesVO.setTags(JSONUtil.toList(resources.getTags(), String.class));
            return resourcesVO;
        }).toList();
        // 关联查询用户信息
        Set<Long> userIdSet = resourcesList.stream().map(Resources::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userNameMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 为每一个资源信息，设置用户信息
        resourcesVOList.forEach(resourcesVO -> {
            Long userId = resourcesVO.getUserId();
            if (userNameMap.containsKey(userId)) {
                User user = userNameMap.get(userId).get(0);
                resourcesVO.setUserVO(userService.getUserVO(user));
            }
        });
        resourcesVOPage.setRecords(resourcesVOList);

        return resourcesVOPage;
    }


    /**
     * 获取查询条件
     *
     * @param resourcesQueryRequest
     * @return
     */
    @Override
    public Wrapper<Resources> getQueryWrapper(ResourcesQueryRequest resourcesQueryRequest) {
        QueryWrapper<Resources> queryWrapper = new QueryWrapper<>();
        if (resourcesQueryRequest == null) {
            return queryWrapper;
        }
        Long id = resourcesQueryRequest.getId();
        String searchText = resourcesQueryRequest.getSearchText();
        String name = resourcesQueryRequest.getName();
        String introduction = resourcesQueryRequest.getIntroduction();
        String category = resourcesQueryRequest.getCategory();
        List<String> tags = resourcesQueryRequest.getTags();
        String region = resourcesQueryRequest.getRegion();
        String userName = resourcesQueryRequest.getUserName();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
                    .or()
                    .like("userName", searchText)
            );
        }
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.like(StrUtil.isNotBlank(region), "region", region);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }


    /**
     * AI一键解释资源
     *
     * @param resourcesId 资源id
     * @param loginUser   登录用户
     * @return
     */
    @Override
    public Flux<String> explainResourcesByChat(Long resourcesId, User loginUser) {
        ResourcesVO resourcesVO = this.getResourcesVOById(resourcesId);
        StringBuilder prompt = new StringBuilder();
        String userPrompt = prompt.append("非遗资源名字为：").append(resourcesVO.getName()).append("\n")
                .append("相关介绍：").append(resourcesVO.getIntroduction()).toString();
        String chatId = UUID.randomUUID().toString();

        Flux<String> chatByStream = resourcesChatApp.doChatByStream(userPrompt, chatId);
        return chatByStream;
    }


    /**
     * 用于校验权限，只有管理员或者本人可以操作
     *
     * @param resources
     * @param loginUser
     */
    public void checkResources(Resources resources, User loginUser) {
        Long userId = loginUser.getId();
        if (!userId.equals(resources.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}




