package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Chat Memory Manager
 * 封装带记忆的对话能力
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * 同步调用（带记忆）
     *
     * @param conversationId 会话ID
     * @param prompt         用户输入
     * @return AI回复
     */
    public String syncCall(String conversationId, String prompt) {
        validateInput(conversationId, prompt);

        log.debug("Memory sync call: conversationId={}, prompt={}", conversationId, truncate(prompt, 100));

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call memory chat: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    /**
     * 流式调用（带记忆）
     *
     * @param conversationId 会话ID
     * @param prompt         用户输入
     * @return AI回复流
     */
    public Flux<String> streamCall(String conversationId, String prompt) {
        validateInput(conversationId, prompt);

        log.debug("Memory stream call: conversationId={}, prompt={}", conversationId, truncate(prompt, 100));

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("Failed to stream call memory chat: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e));
        }
    }

    /**
     * 清除会话记忆
     *
     * @param conversationId 会话ID
     */
    public void clearMemory(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        chatMemory.clear(conversationId);
        log.info("Cleared memory for conversation: {}", conversationId);
    }

    /**
     * 验证输入
     */
    private void validateInput(String conversationId, String prompt) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("输入内容不能为空");
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
