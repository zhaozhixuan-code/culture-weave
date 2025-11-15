package com.zzx.cultureweavebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzx.cultureweavebackend.model.po.ChatMessage;
import com.zzx.cultureweavebackend.service.ChatMessageService;
import com.zzx.cultureweavebackend.utils.KryoSerializationUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
class ChatMessageServiceImplTest {

    @Resource
    private ChatMessageService chatMessageService;

    @Test
    void selectByConversationId() {
        String conversationId = "2027382c-7f86-4dbc-8ef1-b07d5fa196e5";
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversationId", conversationId)
                .orderByDesc("createTime");
        // 从数据库查询信息
        List<ChatMessage> list = chatMessageService.list(queryWrapper);
        List<Message> messageList = list.stream()
                .map(chatMessage -> {
                    Message message = KryoSerializationUtil.<Message>deserializeFromBase64(chatMessage.getMessageData());
                    return message;
                })
                .collect(Collectors.toList());
        Collections.reverse(messageList);
        System.out.println(messageList);
    }
}