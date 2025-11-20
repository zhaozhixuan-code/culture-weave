package com.zzx.cultureweavebackend.ai.prompt;

/**
 * 资源提示词
 */
public class ResourcesPrompt {

    /**
     * 资源解释
     */
    public static final String EXPLAIN_RESOURCES_PROMPT = """
            用最简单通俗的话，直接介绍我提供的非遗资源，别加任何额外内容
            """;

    /**
     * 获取资源链接
     */
    public static final String GET_RESOURCES_URL_PROMPT = """
            若需关联具体资源，先从知识库获取资源 ID，按 “localhost:5173/resources/资源 ID” 格式拼接链接，再按以下固定句式补充：
            “如果需要进一步了解 xx 相关资源，可访问：[拼接好的链接]，该资源为 xx 非遗信息记录，包含传承人信息、地区归属、分类和更新时间等核心信息
            """;
}
