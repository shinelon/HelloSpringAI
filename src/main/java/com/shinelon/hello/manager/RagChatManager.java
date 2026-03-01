package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    private ChatClient simpleRagClient;
    private ChatClient advancedRagClient;

    private static final List<String> DOCUMENT_SOURCES = List.of(
            "公司介绍", "产品说明", "技术架构", "常见问题", "联系方式"
    );

    @PostConstruct
    public void init() {
        initSimpleRagClient();
        initAdvancedRagClient();
        log.info("[RAG] RagChatManager 初始化完成");
    }

    private void initSimpleRagClient() {
        this.simpleRagClient = chatClientBuilder.clone()
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.5)
                                .topK(3)
                                .build())
                        .build())
                .build();
        log.info("[RAG] 简单版 RAG 客户端初始化完成 (QuestionAnswerAdvisor)");
    }

    private void initAdvancedRagClient() {
        var advancedAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(3)
                        .build())
                .build();

        this.advancedRagClient = chatClientBuilder.clone()
                .defaultAdvisors(advancedAdvisor)
                .build();
        log.info("[RAG] 进阶版 RAG 客户端初始化完成 (RetrievalAugmentationAdvisor + RewriteQueryTransformer)");
    }

    public String simpleChat(String query) {
        validateInput(query);
        log.debug("[RAG-Simple] 查询: {}", truncate(query, 100));

        try {
            return simpleRagClient.prompt()
                    .user(query)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Simple] 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e);
        }
    }

    public Flux<String> simpleChatStream(String query) {
        validateInput(query);
        log.debug("[RAG-Simple-Stream] 查询: {}", truncate(query, 100));

        try {
            return simpleRagClient.prompt()
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Simple-Stream] 调用失败: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e));
        }
    }

    public String advancedChat(String query) {
        validateInput(query);
        log.debug("[RAG-Advanced] 查询: {}", truncate(query, 100));

        try {
            return advancedRagClient.prompt()
                    .user(query)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Advanced] 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e);
        }
    }

    public Flux<String> advancedChatStream(String query) {
        validateInput(query);
        log.debug("[RAG-Advanced-Stream] 查询: {}", truncate(query, 100));

        try {
            return advancedRagClient.prompt()
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Advanced-Stream] 调用失败: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e));
        }
    }

    public List<String> getDocumentSources() {
        return DOCUMENT_SOURCES;
    }

    private void validateInput(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
