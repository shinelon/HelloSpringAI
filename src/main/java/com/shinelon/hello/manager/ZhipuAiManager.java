package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 智谱AI Manager
 * 封装智谱AI调用能力
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ZhipuAiManager {

    private final ChatClient.Builder chatClientBuilder;

    private ChatClient chatClient;

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 同步调用
     *
     * @param prompt 用户输入
     * @return AI回复
     */
    public String syncCall(String prompt) {
        validatePrompt(prompt);

        log.debug("Sync call with prompt: {}", truncate(prompt, 100));

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call ZhipuAI: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    /**
     * 带系统提示的同步调用
     *
     * @param systemPrompt 系统提示
     * @param userPrompt   用户输入
     * @return AI回复
     */
    public String syncCall(String systemPrompt, String userPrompt) {
        validatePrompt(userPrompt);

        log.debug("Sync call with system prompt and user prompt");

        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call ZhipuAI: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    /**
     * 带历史消息的同步调用
     *
     * @param messages 消息列表（包含历史）
     * @return AI回复
     */
    public String syncCallWithHistory(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        log.debug("Sync call with {} messages", messages.size());

        try {
            return chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call ZhipuAI with history: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    /**
     * 流式调用
     *
     * @param prompt 用户输入
     * @return AI回复流
     */
    public Flux<String> streamCall(String prompt) {
        validatePrompt(prompt);

        log.debug("Stream call with prompt: {}", truncate(prompt, 100));

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("Failed to stream call ZhipuAI: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e));
        }
    }

    /**
     * 带历史消息的流式调用
     *
     * @param messages 消息列表（包含历史）
     * @return AI回复流
     */
    public Flux<String> streamCallWithHistory(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("消息列表不能为空");
        }

        log.debug("Stream call with {} messages", messages.size());

        try {
            return chatClient.prompt()
                    .messages(messages)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("Failed to stream call ZhipuAI with history: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e));
        }
    }

    /**
     * 创建用户消息
     */
    public UserMessage createUserMessage(String content) {
        return new UserMessage(content);
    }

    /**
     * 创建系统消息
     */
    public SystemMessage createSystemMessage(String content) {
        return new SystemMessage(content);
    }

    /**
     * 创建助手消息
     */
    public AssistantMessage createAssistantMessage(String content) {
        return new AssistantMessage(content);
    }

    /**
     * 验证输入
     */
    private void validatePrompt(String prompt) {
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
