package com.zzx.cultureweavebackend.ai.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量数据库配置
 */
@Configuration
public class VectorStoreConfig {

    @Resource
    private MySQLDocumentReader mysqlDocumentReader;

    @Resource
    private KeywordEnricher keywordEnricher;

    @Bean
    VectorStore vectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 从数据库获取documents文件
        List<Document> documents = mysqlDocumentReader.loadMySqlDocuments();
        // 自动补充关键元信息
        List<Document> enrichDocuments = keywordEnricher.enrichDocuments(documents);
        // 添加文件到向量存储
        simpleVectorStore.add(enrichDocuments);
        return simpleVectorStore;
    }
}
