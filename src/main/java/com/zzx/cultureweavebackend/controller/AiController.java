package com.zzx.cultureweavebackend.controller;

import com.zzx.cultureweavebackend.ai.ResourcesChatApp;
import com.zzx.cultureweavebackend.exception.ErrorCode;
import com.zzx.cultureweavebackend.exception.ThrowUtils;
import com.zzx.cultureweavebackend.model.po.User;
import com.zzx.cultureweavebackend.service.ResourcesService;
import com.zzx.cultureweavebackend.service.UserService;
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
    private ResourcesChatApp resourcesChatApp;

    @Resource
    private UserService userService;

    @Resource
    private ResourcesService resourcesService;


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
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return resourcesChatApp.doChatWithRAGByStream(message, chatId);
    }


    /**
     * 利用AI一键解释资源接口
     * @param resourcesId 资源id
     * @param request
     * @return
     */
    @GetMapping(value = "/explain/resources", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> explainResourcesByChat(
            @RequestParam("resourcesId") Long resourcesId,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        return resourcesService.explainResourcesByChat(resourcesId, loginUser);
    }
}
