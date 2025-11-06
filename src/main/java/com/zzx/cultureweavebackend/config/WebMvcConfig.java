package com.zzx.cultureweavebackend.config;

import com.zzx.cultureweavebackend.constants.FileConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置（与 FileConstant 完全对齐，避免配置冲突）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 公开图片目录映射：URL -> 本地路径
        // URL: /public-images/** -> 本地: 项目根目录/image/public-images/
        String publicLocalPath = FileConstant.FILE_SAVE_DIR + File.separator + FileConstant.PUBLIC_SUB_DIR;
        registry.addResourceHandler(FileConstant.PUBLIC_ACCESS_PREFIX + "**")
                .addResourceLocations("file:" + publicLocalPath + File.separator)
                .setCachePeriod(3600); // 1小时缓存优化

        // 2. 资源图片目录映射（公开访问，与用户配置一致）
        // URL: /resource-image/** -> 本地: 项目根目录/image/resource-image/
        String resourceLocalPath = FileConstant.FILE_SAVE_DIR + File.separator + FileConstant.RESOURCE_SUB_DIR;
        registry.addResourceHandler(FileConstant.RESOURCE_ACCESS_PREFIX + "**")
                .addResourceLocations("file:" + resourceLocalPath + File.separator)
                .setCachePeriod(3600);
    }
}