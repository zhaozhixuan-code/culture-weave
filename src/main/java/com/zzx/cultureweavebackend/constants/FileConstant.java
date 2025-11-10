package com.zzx.cultureweavebackend.constants;

import java.io.File;

/**
 * 文件常量类（统一路径配置，适配跨系统）
 */
public class FileConstant {


    public static final String URL = "http://localhost:8123";

    /**
     * 图片上传根目录：项目根目录下的 image 文件夹
     * File.separator 自动适配 Windows(\) 和 Linux/Mac(/)
     */
    public static final String FILE_SAVE_DIR = System.getProperty("user.dir") + File.separator + "images";

    /**
     * 公开图片子目录（与实际文件夹名一致）
     */
    public static final String PUBLIC_SUB_DIR = "public-images";

    /**
     * 业务资源图片子目录（与实际文件夹名一致）
     */
    public static final String RESOURCE_SUB_DIR = "resource-images";

    /**
     * 公开图片访问URL前缀（与 WebMvcConfig 映射规则对齐）
     */
    public static final String PUBLIC_ACCESS_PREFIX = "/public-images/";

    /**
     * 资源图片访问URL前缀（与 WebMvcConfig 映射规则对齐）
     */
    public static final String RESOURCE_ACCESS_PREFIX = "/resource-image/";
}