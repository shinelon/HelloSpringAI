package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.manager.RagChatManager;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import com.shinelon.hello.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG 对话服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final RagChatManager ragChatManager;

    @Override
    public RagChatVO simpleChat(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[simpleChat] 简单版RAG对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        String content = ragChatManager.simpleChat(request.getQuery());

        log.info("[simpleChat] 简单版RAG对话完成, 响应长度={}", content.length());

        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[simpleChatStream] 简单版RAG流式对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        return ragChatManager.simpleChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("[simpleChatStream] 流式对话错误: {}", e.getMessage(), e));
    }

    @Override
    public RagChatVO advancedChat(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[advancedChat] 进阶版RAG对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        String content = ragChatManager.advancedChat(request.getQuery());

        log.info("[advancedChat] 进阶版RAG对话完成, 响应长度={}", content.length());

        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[advancedChatStream] 进阶版RAG流式对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        return ragChatManager.advancedChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("[advancedChatStream] 流式对话错误: {}", e.getMessage(), e));
    }

    @Override
    public List<String> getDocuments() {
        return ragChatManager.getDocumentSources();
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(RagChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }
}
