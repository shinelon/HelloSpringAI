package com.shinelon.hello.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat Memory 配置类
 * 使用 InMemory 存储实现对话记忆功能
 *
 * @author shinelon
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 配置 ChatMemory Bean
     * 使用内存存储，保留最近20条消息
     *
     * @return ChatMemory 实例
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }
}
