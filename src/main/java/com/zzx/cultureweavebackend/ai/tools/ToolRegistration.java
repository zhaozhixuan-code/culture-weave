package com.zzx.cultureweavebackend.ai.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册类
 */
@Configuration
public class ToolRegistration {

    // search-api 用于百度搜索
    @Value("${searchapi.api-key}")
    private String searchApiKey;



    @Bean
    public ToolCallback[] allTools() {
        // 联网搜索
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        // 网页抓取
        WebScrapingTool webScrapingTool = new WebScrapingTool();

        return ToolCallbacks.from(
                webSearchTool,
                webScrapingTool
        );
    }
}
