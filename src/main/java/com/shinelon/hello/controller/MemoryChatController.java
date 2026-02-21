package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.model.dto.MemoryChatRequestDTO;
import com.shinelon.hello.model.vo.MemoryChatVO;
import com.shinelon.hello.service.MemoryChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Chat Memory 控制器
 * 提供带记忆的对话接口
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/learn")
@RequiredArgsConstructor
public class MemoryChatController {

    private final MemoryChatService memoryChatService;

    /**
     * 带记忆的同步对话
     *
     * @param request 请求参数
     * @return 响应
     */
    @PostMapping("/memory/chat")
    public Result<MemoryChatVO> chat(@Valid @RequestBody MemoryChatRequestDTO request) {
        log.info("Memory chat request: conversationId={}, content={}",
                request.getConversationId(),
                truncate(request.getContent(), 50));

        MemoryChatVO response = memoryChatService.chat(request);
        return Result.success(response);
    }

    /**
     * 带记忆的流式对话
     *
     * @param request 请求参数
     * @return SSE 流
     */
    @PostMapping(value = "/memory/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<MemoryChatVO>> chatStream(@Valid @RequestBody MemoryChatRequestDTO request) {
        log.info("Memory stream chat request: conversationId={}, content={}",
                request.getConversationId(),
                truncate(request.getContent(), 50));

        String conversationId = request.getConversationId();

        return memoryChatService.chatStream(request)
                .map(message -> ServerSentEvent.<MemoryChatVO>builder()
                        .data(message)
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<MemoryChatVO>builder()
                                .data(MemoryChatVO.builder()
                                        .conversationId(conversationId)
                                        .content("")
                                        .build())
                                .event("done")
                                .build()
                ))
                .doOnError(e -> log.error("Memory stream error: {}", e.getMessage(), e));
    }

    /**
     * 清除会话记忆
     *
     * @param conversationId 会话ID
     * @return 响应
     */
    @DeleteMapping("/memory/{conversationId}")
    public Result<Void> clearMemory(@PathVariable String conversationId) {
        log.info("Clear memory request: conversationId={}", conversationId);
        memoryChatService.clearMemory(conversationId);
        return Result.success();
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
