package com.zzx.cultureweavebackend.ai.rag;

import com.zzx.cultureweavebackend.model.po.Resources;
import com.zzx.cultureweavebackend.service.ResourcesService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 基于Mysql的文档读取器
 */
@Component
public class MySQLDocumentReader {

    @Resource
    private ResourcesService resourcesService;


    public List<Document> loadMySqlDocuments() {
        List<Document> documents = new ArrayList<>();

        // 获取所有
        List<Resources> resourcesList = resourcesService.list();

        // 将每个 ChatMessage 转换为 Document
        for (Resources resources : resourcesList) {
            String content = buildContent(resources);
            documents.add(new Document(content));
        }
        return documents;
    }

    private String buildContent(Resources resources) {
        // 构建内容
        StringBuilder content = new StringBuilder();
        // 获取所有的属性
        Long resourcesId = resources.getId();
        String name = resources.getName();
        String introduction = resources.getIntroduction();
        String category = resources.getCategory();
        String tags = resources.getTags();
        String region = resources.getRegion();
        Integer price = resources.getPrice();
        String userName = resources.getUserName();
        Date updateTime = resources.getUpdateTime();
        content.append("非遗资源名字: ").append(name).append("\n")
                .append("非遗资源描述: ").append(introduction).append("\n")
                .append("非遗分类: ").append(category).append("\n")
                .append("非遗标签: ").append(tags).append("\n")
                .append("非遗地区: ").append(region).append("\n")
                .append("非遗资源授权价格: ").append(price).append("\n")
                .append("非遗传承人: ").append(userName).append("\n")
                .append("更新时间: ").append(updateTime).append("\n")
                .append("资源id: ").append(resourcesId);

        return content.toString();
    }


}
