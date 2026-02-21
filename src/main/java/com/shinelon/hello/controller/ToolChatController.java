package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.model.dto.ToolChatRequestDTO;
import com.shinelon.hello.model.vo.ToolChatVO;
import com.shinelon.hello.service.ToolChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Tool Calling 控制器
 * 提供带工具调用的对话接口
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/learn")
@RequiredArgsConstructor
public class ToolChatController {

    private final ToolChatService toolChatService;

    /**
     * 带工具的同步对话
     *
     * @param request 请求参数
     * @return 响应
     */
    @PostMapping("/tool/chat")
    public Result<ToolChatVO> chat(@Valid @RequestBody ToolChatRequestDTO request) {
        log.info("Tool chat request: content={}, enabledTools={}",
                DesensitizationUtils.truncateAndMask(request.getContent(), 50),
                request.getEnabledTools());

        ToolChatVO response = toolChatService.chat(request);
        return Result.success(response);
    }

    /**
     * 带工具的流式对话
     *
     * @param request 请求参数
     * @return SSE 流
     */
    @PostMapping(value = "/tool/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ToolChatVO>> chatStream(@Valid @RequestBody ToolChatRequestDTO request) {
        log.info("Tool stream chat request: content={}, enabledTools={}",
                DesensitizationUtils.truncateAndMask(request.getContent(), 50),
                request.getEnabledTools());

        return toolChatService.chatStream(request)
                .map(message -> ServerSentEvent.<ToolChatVO>builder()
                        .data(message)
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<ToolChatVO>builder()
                                .data(ToolChatVO.builder()
                                        .content("")
                                        .build())
                                .event("done")
                                .build()
                ))
                .doOnError(e -> log.error("Tool stream error: {}", e.getMessage(), e));
    }

    /**
     * 获取可用的工具列表
     *
     * @return 工具列表
     */
    @GetMapping("/tool/list")
    public Result<List<String>> listTools() {
        log.info("Get available tools");
        List<String> tools = toolChatService.getAvailableTools();
        return Result.success(tools);
    }
}
