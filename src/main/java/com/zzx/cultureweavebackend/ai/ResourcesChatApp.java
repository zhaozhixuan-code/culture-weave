package com.zzx.cultureweavebackend.ai;

import com.zzx.cultureweavebackend.ai.advisor.MyLoggerAdvisor;
import com.zzx.cultureweavebackend.ai.chatmemory.MysqlChatMemory;
import com.zzx.cultureweavebackend.ai.prompt.ResourcesPrompt;
import com.zzx.cultureweavebackend.ai.prompt.SystemPrompt;
import com.zzx.cultureweavebackend.ai.rag.QueryRewriter;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


/**
 * ai对话聊天应用
 */
@Component
public class ResourcesChatApp {
    /**
     * 聊天客户端
     */
    private final ChatClient chatClient;

    /**
     * 定义系统提示
     */
    private final String SYSTEM_PROMPT = SystemPrompt.SYSTEM_ROLE_PROMPT;

    /**
     * 向量数据库
     */
    @Resource
    private VectorStore vectorStore;

    /**
     * 查询重写器
     */
    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 工具
     */
    @Resource
    private ToolCallback[] allTools;


    /**
     * 初始化创建应用
     */
    public ResourcesChatApp(ChatModel dashscopeChatModel, MysqlChatMemory mysqlChatMemory) {

        // 创建聊天客户端
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 基于mysql的会话存储
                        MessageChatMemoryAdvisor.builder(mysqlChatMemory).build(),
                        // 自定义日志拦截器
                        new MyLoggerAdvisor()
                        // 重读,提高大模型的推理能力
                        // new ReReadingAdvisor()
                )
                .build();

    }

    /**
     * 一键问 AI 功能
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        // 加载提示词
        String prompt = ResourcesPrompt.EXPLAIN_RESOURCES_PROMPT;
        Flux<String> stringFlux = chatClient.prompt(prompt)
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
        return stringFlux;
    }

    /**
     * AI RAG知识库 + 内存向量数据库
     *
     * @param message 用户的提示词
     * @param chatId  对话id
     * @return AI 的回复
     */
    public String doChatWithRAG(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        String prompt = ResourcesPrompt.GET_RESOURCES_URL_PROMPT;
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 使用向量数据库
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    public Flux<String> doChatWithRAGByStream(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        String prompt = ResourcesPrompt.GET_RESOURCES_URL_PROMPT;
        Flux<String> stringFlux = chatClient.prompt(prompt)
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 使用向量数据库
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .toolCallbacks(allTools)
                .stream()
                .content();
        return stringFlux;
    }
}
