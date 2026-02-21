package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.model.dto.ChatRequestDTO;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天控制器
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 同步对话接口
     *
     * @param request 对话请求
     * @return 消息响应
     */
    @PostMapping("/chat")
    public Result<MessageVO> chat(@Valid @RequestBody ChatRequestDTO request) {
        log.info("Chat request: sessionId={}, content={}",
                DesensitizationUtils.maskId(request.getSessionId()),
                DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        MessageVO response = chatService.chat(request);
        return Result.success(response);
    }

    /**
     * 流式对话接口
     *
     * @param request 对话请求
     * @return SSE 流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<MessageVO>> chatStream(@Valid @RequestBody ChatRequestDTO request) {
        log.info("Stream chat request: sessionId={}, content={}",
                DesensitizationUtils.maskId(request.getSessionId()),
                DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        String sessionId = request.getSessionId();

        return chatService.chatStream(request)
                .map(message -> ServerSentEvent.<MessageVO>builder()
                        .data(message)
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<MessageVO>builder()
                                .data(MessageVO.builder()
                                        .sessionId(sessionId)
                                        .content("")
                                        .build())
                                .event("done")
                                .build()
                ))
                .doOnError(e -> log.error("Stream error: {}", e.getMessage(), e));
    }
}
