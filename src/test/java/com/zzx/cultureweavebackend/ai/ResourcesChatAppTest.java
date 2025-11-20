package com.zzx.cultureweavebackend.ai;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ResourcesChatAppTest {

    @Resource
    private ResourcesChatApp resourcesChatApp;

    @Test
    void doChatWithRAG() {
        String chatId = UUID.randomUUID().toString();
        System.out.println(resourcesChatApp.doChatWithRAG("苏绣发源地在哪", chatId));
        // System.out.println(chatApp.doChat("我叫什么来着", chatId));
    }
}