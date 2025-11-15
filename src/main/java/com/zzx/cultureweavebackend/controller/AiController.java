package com.zzx.cultureweavebackend.controller;

import com.zzx.cultureweavebackend.ai.ChatApp;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI相关接口
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ChatApp chatApp;



    /**
     * AI对话接口
     * @param message 用户输入的提示词
     * @param chatId  对话id
     * @return AI 的回复
     */
    @GetMapping(value = "/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithSSE(
            @RequestParam("message") String message,
            @RequestParam("chatId") String chatId,
            HttpServletRequest  request) {

        System.out.println(">>> 收到 message = [" + message + "], chatId = [" + chatId + "]");
        return chatApp.doChatByStream(message, chatId);
    }
}
