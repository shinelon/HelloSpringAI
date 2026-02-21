package com.shinelon.hello.service.impl;

import com.shinelon.hello.manager.MemoryChatManager;
import com.shinelon.hello.model.dto.MemoryChatRequestDTO;
import com.shinelon.hello.model.vo.MemoryChatVO;
import com.shinelon.hello.service.MemoryChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * Chat Memory 服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryChatServiceImpl implements MemoryChatService {

    private final MemoryChatManager memoryChatManager;

    @Override
    public MemoryChatVO chat(MemoryChatRequestDTO request) {
        validateRequest(request);

        log.info("Memory chat: conversationId={}", request.getConversationId());

        String response = memoryChatManager.syncCall(
                request.getConversationId(),
                request.getContent()
        );

        return MemoryChatVO.builder()
                .conversationId(request.getConversationId())
                .content(response)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<MemoryChatVO> chatStream(MemoryChatRequestDTO request) {
        validateRequest(request);

        log.info("Memory stream chat: conversationId={}", request.getConversationId());

        String conversationId = request.getConversationId();

        return memoryChatManager.streamCall(conversationId, request.getContent())
                .map(chunk -> MemoryChatVO.builder()
                        .conversationId(conversationId)
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("Memory stream chat error: {}", e.getMessage(), e));
    }

    @Override
    public void clearMemory(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.info("Clearing memory: conversationId={}", conversationId);
        memoryChatManager.clearMemory(conversationId);
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(MemoryChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getConversationId() == null || request.getConversationId().trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}
