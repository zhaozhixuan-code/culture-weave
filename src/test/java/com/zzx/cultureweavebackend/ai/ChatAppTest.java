package com.zzx.cultureweavebackend.ai;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatAppTest {

    @Resource
    private ChatApp chatApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        System.out.println(chatApp.doChat("苏绣发源地在哪", chatId));
        // System.out.println(chatApp.doChat("我叫什么来着", chatId));
    }
}