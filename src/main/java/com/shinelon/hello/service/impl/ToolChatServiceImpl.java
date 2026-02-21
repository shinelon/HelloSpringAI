package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.manager.ToolChatManager;
import com.shinelon.hello.model.dto.ToolChatRequestDTO;
import com.shinelon.hello.model.vo.ToolChatVO;
import com.shinelon.hello.service.ToolChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tool Calling 服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolChatServiceImpl implements ToolChatService {

    private final ToolChatManager toolChatManager;

    @Override
    public ToolChatVO chat(ToolChatRequestDTO request) {
        validateRequest(request);

        log.info("[chat] Tool调用请求开始, enabledTools={}, content={}",
                request.getEnabledTools(), DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        String response = toolChatManager.syncCall(
                request.getContent(),
                request.getEnabledTools()
        );

        log.info("[chat] Tool调用完成, 响应长度={}", response.length());

        return ToolChatVO.builder()
                .content(response)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<ToolChatVO> chatStream(ToolChatRequestDTO request) {
        validateRequest(request);

        log.info("[chatStream] Tool流式调用开始, enabledTools={}, content={}",
                request.getEnabledTools(), DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        return toolChatManager.streamCall(request.getContent(), request.getEnabledTools())
                .map(chunk -> ToolChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("[chatStream] Tool流式调用错误, error={}", e.getMessage(), e));
    }

    @Override
    public List<String> getAvailableTools() {
        return toolChatManager.getAvailableTools();
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(ToolChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}
