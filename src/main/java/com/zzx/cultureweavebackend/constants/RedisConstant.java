package com.zzx.cultureweavebackend.constants;

/**
 * Redis常量
 */
public interface RedisConstant {

    /**
     * 资源-热门搜索词
     */
    String RESOURCES_HOT_SEARCH_TEXT = "resources_hot_search_text";

    /**
     * 缓存热门搜索结果
     */
    String RESOURCES_SELECT = "resources:select:";

    /**
     * 用户搜索记录
     */
    String USER_SEARCH_HISTORY = "user:search:history:";
}
