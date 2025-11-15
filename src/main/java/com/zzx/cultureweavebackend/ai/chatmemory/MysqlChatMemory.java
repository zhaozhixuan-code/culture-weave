package com.zzx.cultureweavebackend.ai.chatmemory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzx.cultureweavebackend.model.po.ChatMessage;
import com.zzx.cultureweavebackend.service.ChatMessageService;
import com.zzx.cultureweavebackend.utils.BaseContext;
import com.zzx.cultureweavebackend.utils.KryoSerializationUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于Mysql的对话记忆持久化实现
 */
@Component
public class MysqlChatMemory implements ChatMemory {

    /**
     * 注入聊天消息服务
     */
    @Resource
    private ChatMessageService chatMessageService;


    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> messageList = messages.stream()
                .map(message -> {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setConversationId(conversationId);
                    chatMessage.setMessageData(KryoSerializationUtil.serializeToBase64(message));
                    chatMessage.setUserId(BaseContext.getCurrentId());
                    return chatMessage;
                }).collect(Collectors.toList());
        chatMessageService.saveBatch(messageList, messageList.size());
    }

    @Override
    public List<Message> get(String conversationId) {
        // 根据会话id查询聊天消息
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversationId", conversationId)
                .orderByDesc("createTime");
        // 从数据库查询信息
        List<ChatMessage> list = chatMessageService.list(queryWrapper);
        // 按照时间顺序返回
        if (CollUtil.isNotEmpty(list)) {
            Collections.reverse(list);
        }

        List<Message> messageList = list.stream()
                .map(chatMessage -> {
                    Message message = KryoSerializationUtil.<Message>deserializeFromBase64(chatMessage.getMessageData());
                    return message;
                })
                .collect(Collectors.toList());

        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversationId", conversationId);
        chatMessageService.remove(queryWrapper);
    }

}
