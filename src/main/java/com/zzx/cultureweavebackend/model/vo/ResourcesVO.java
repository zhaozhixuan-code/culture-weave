package com.zzx.cultureweavebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.zzx.cultureweavebackend.model.po.Resources;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ResourcesVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 地区
     */
    private String region;

    /**
     * 资源图片
     */
    private String resourceImgUrl;

    /**
     * 授权价格
     */
    private Integer price;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 创建用户姓名
     */
    private String userName;


    /**
     * 对象转包装类
     * @param resources
     * @return
     */
    public static ResourcesVO objToVo(Resources resources) {
        if(resources == null){
            return null;
        }
        ResourcesVO resourcesVO = new ResourcesVO();
        BeanUtil.copyProperties(resources, resourcesVO);
        resourcesVO.setTags(JSONUtil.toList(resources.getTags(), String.class));
        return resourcesVO;
    }

    /**
     * 包装类转对象
     * @param resourcesVO
     * @return
     */
    public static Resources voToObj(ResourcesVO resourcesVO) {
        if(resourcesVO == null){
            return null;
        }
        Resources resources = new Resources();
        BeanUtil.copyProperties(resourcesVO, resources);
        resources.setTags(JSONUtil.toJsonStr(resourcesVO.getTags()));
        return resources;
    }

}
