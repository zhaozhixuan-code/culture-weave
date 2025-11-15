package com.zzx.cultureweavebackend.ai.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;


import java.util.List;

/**
 * 基于AI自动解析关键词并添加到原信息中
 */
@Component
public class KeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 添加关键词
     * @param documents
     * @return
     */
    List<Document> enrichDocuments(List<Document> documents) {
        // 提取5个关键词
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.dashscopeChatModel, 5);
        return enricher.apply(documents);

    }
}
