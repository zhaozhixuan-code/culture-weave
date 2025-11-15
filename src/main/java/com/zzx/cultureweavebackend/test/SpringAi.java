package com.zzx.cultureweavebackend.test;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// @Component
public class SpringAi implements CommandLineRunner {

    // 创建一个 ChatModel 对象
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) {
        List<Generation> results = dashscopeChatModel.call(new Prompt("你好，请介绍一下自己"))
                .getResults();
        for (Generation result : results) {
            System.out.println(result);
        }

    }


}
