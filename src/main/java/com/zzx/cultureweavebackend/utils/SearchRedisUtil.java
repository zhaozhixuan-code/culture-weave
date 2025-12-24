package com.zzx.cultureweavebackend.utils;

import com.zzx.cultureweavebackend.constants.RedisConstant;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis 获取热搜和最近搜索工具类
 */
@Component
public class SearchRedisUtil {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 设置热搜的过期时间
     */
    @PostConstruct
    public void setHotSearchExpireTime() {
        redisTemplate.expire(RedisConstant.RESOURCES_HOT_SEARCH_TEXT, 10, TimeUnit.SECONDS);
    }

    /**
     * 添加热搜词
     *
     * @param searchText 热搜列表
     */
    public void addHotSearch(String searchText) {
        //1：表示每调用一次，当前product的分数+1
        redisTemplate.opsForZSet().incrementScore(RedisConstant.RESOURCES_HOT_SEARCH_TEXT, searchText, 1D);
    }

    /**
     * 获取热搜词列表
     *
     * @return 热搜列表
     */
    public Set<String> listHotSearch() {
        return redisTemplate.opsForZSet().reverseRange(RedisConstant.RESOURCES_HOT_SEARCH_TEXT, 0, 10);
    }

    /**
     * 添加用户最近搜索
     *
     * @param userId
     * @param searchText
     */
    public void addRecentSearch(Long userId, String searchText) {
        // 1. 获取 Redis Key
        String keyword = searchText.trim();
        String redisKey = RedisConstant.USER_SEARCH_HISTORY + userId;
        // 2. Sorted Set 操作：添加/更新关键词（score 为当前时间戳，实现去重+排序）
        redisTemplate.opsForZSet().add(redisKey, keyword, System.currentTimeMillis());
        // 3. 限制最大条数：超过 MAX_HISTORY_COUNT 时，删除最早的记录（score 最小的）
        long currentSize = redisTemplate.opsForZSet().size(redisKey);
        if (currentSize > 10) {
            // 删除 score 最小的（currentSize - MAX_HISTORY_COUNT）条记录
            redisTemplate.opsForZSet().removeRange(redisKey, 0, currentSize - 10 - 1);
        }
    }

    /**
     * 查询用户搜索历史（按搜索时间倒序，最新在前）
     * @param userId 用户ID
     * @return 关键词列表（倒序）
     */
    public List<String> getSearchHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        String redisKey = RedisConstant.USER_SEARCH_HISTORY + userId;
        // Sorted Set 倒序查询（score 从大到小）：0 到 -1 表示查询所有
        Set<Object> keywordSet = redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1);
        // 转换为 List<String> 并返回（空集合时返回空列表）
        return keywordSet == null ? Collections.emptyList() :
                keywordSet.stream().map(String::valueOf).collect(Collectors.toList());
    }

}