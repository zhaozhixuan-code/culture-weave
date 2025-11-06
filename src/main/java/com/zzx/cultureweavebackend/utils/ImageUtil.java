package com.zzx.cultureweavebackend.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import com.zzx.cultureweavebackend.constants.FileConstant;
import com.zzx.cultureweavebackend.exception.BusinessException;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.enums.FileUploadPathEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;


/**
 * 图片处理工具类
 */
@Slf4j
@Component
public class ImageUtil {

    // 非遗资源目录
    public static final String FILE_RESOURCE_DIR = FileConstant.FILE_SAVE_DIR + File.separator + FileConstant.RESOURCE_SUB_DIR;
    // 创作中心保存目录
    public static final String FILE_PUBLIC_DIR = FileConstant.FILE_SAVE_DIR + File.separator + FileConstant.PUBLIC_SUB_DIR;

    static {
        // 静态初始化块，确保保存目录存在
        createDirectoryIfNotExists();
    }

    /**
     * 确保文件保存目录存在，如果不存在则创建
     */
    public static void createDirectoryIfNotExists() {
        File directory = new File(FILE_RESOURCE_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("文件保存目录已创建: {}", FILE_RESOURCE_DIR);
            } else {
                log.info("创建文件保存目录失败: {}", FILE_RESOURCE_DIR);
            }
        }
        directory = new File(FILE_PUBLIC_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("文件保存目录已创建: {}", FILE_PUBLIC_DIR);
            } else {
                log.info("创建文件保存目录失败: {}", FILE_PUBLIC_DIR);
            }
        }
    }


    /**
     * 保存图片并返回前端可访问URL
     *
     * @param file             上传文件
     * @param uploadPathPrefix 上传目录枚举（public-images/resource-image）
     * @return 访问URL：如 /public-images/xxx.jpg 或 /resource-image/xxx.jpg
     */
    public String saveImage(MultipartFile file, FileUploadPathEnum uploadPathPrefix) {
        // 1. 校验文件合法性（空文件、大小、格式）
        check(file);

        // 3. 生成唯一文件名
        String fileName = DateUtil.formatDate(new Date()) + "_" + RandomUtil.randomString(16) + "_" + file.getOriginalFilename();
        String targetDir;
        switch (uploadPathPrefix) {
            case PUBLIC -> targetDir = FILE_PUBLIC_DIR;
            case RESOURCE -> targetDir = FILE_RESOURCE_DIR;
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的上传目录类型");
        }

        // 4. 拼接完整本地路径
        String localFilePath = targetDir + File.separator + fileName;
        File targetFile = FileUtil.file(localFilePath); // 仅用于路径封装，无复杂依赖

        // 5. 保存文件到本地
        try {
            FileUtil.writeBytes(file.getBytes(), targetFile);
            log.info("图片保存成功，本地路径：{}", localFilePath);
        } catch (Exception e) {
            log.error("图片保存失败，本地路径：{}，异常信息：{}", localFilePath, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败，请重试");
        }

        // 6. 生成并返回前端访问URL
        return buildAccessUrl(uploadPathPrefix, fileName);
    }


    /**
     * 构建前端访问URL（与 WebMvcConfig 资源映射对齐）
     */
    private String buildAccessUrl(FileUploadPathEnum uploadPathPrefix, String fileName) {
        // 根据上传目录选择对应的URL前缀
        String accessPrefix = switch (uploadPathPrefix) {
            case PUBLIC -> FileConstant.PUBLIC_ACCESS_PREFIX;
            case RESOURCE -> FileConstant.RESOURCE_ACCESS_PREFIX;
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的上传目录类型");
        };
        // 拼接URL（如 /public-images/xxx.jpg）
        log.info("构建访问URL：{}", accessPrefix + fileName);
        return accessPrefix + fileName;
    }

    /**
     * 检查图片
     *
     * @param file
     */
    private void check(MultipartFile file) {
        // 判断图片是否为空
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "图片不能为空");
        // 校验图片大小不超过10MB
        long MaxSize = 1024 * 1024 * 10;
        ThrowUtils.throwIf(file.getSize() > MaxSize, ErrorCode.PARAMS_ERROR, "图片大小不能超过10MB");
        // 校验图片格式，只允许jpg、png、jpeg格式
        String[] allowedExtensions = {"jpg", "jpeg", "png", "bmp", "webp"};
        String fileName = file.getOriginalFilename();
        // 获取文件后缀
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        // 判断文件后缀是否在允许的列表中
        ThrowUtils.throwIf(!ArrayUtil.contains(allowedExtensions, extension),
                ErrorCode.PARAMS_ERROR, "图片格式错误");
    }

}