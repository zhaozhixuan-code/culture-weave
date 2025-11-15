package com.zzx.cultureweavebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.cultureweavebackend.model.po.ChatMessage;
import com.zzx.cultureweavebackend.service.ChatMessageService;
import com.zzx.cultureweavebackend.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
 * @author 28299
 * @description 针对表【chat_message(ai对话信息存储表)】的数据库操作Service实现
 * @createDate 2025-11-12 22:52:56
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {



}




