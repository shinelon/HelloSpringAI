package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import com.shinelon.hello.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 对话控制器
 * 提供简单版和进阶版 RAG 对话 API
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/learn/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // ==================== 简单版 RAG ====================

    /**
     * 简单版 RAG 同步对话
     * 使用 QuestionAnswerAdvisor
     */
    @PostMapping("/simple/chat")
    public Result<RagChatVO> simpleChat(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[simpleChat] API调用, query={}", request.getQuery());
        return Result.success(ragService.simpleChat(request));
    }

    /**
     * 简单版 RAG 流式对话 (SSE)
     * 使用 QuestionAnswerAdvisor
     */
    @PostMapping("/simple/chat/stream")
    public Flux<RagChatVO> simpleChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[simpleChatStream] API调用, query={}", request.getQuery());
        return ragService.simpleChatStream(request);
    }

    // ==================== 进阶版 RAG ====================

    /**
     * 进阶版 RAG 同步对话
     * 使用 RetrievalAugmentationAdvisor + 查询改写
     */
    @PostMapping("/advanced/chat")
    public Result<RagChatVO> advancedChat(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[advancedChat] API调用, query={}", request.getQuery());
        return Result.success(ragService.advancedChat(request));
    }

    /**
     * 进阶版 RAG 流式对话 (SSE)
     * 使用 RetrievalAugmentationAdvisor + 查询改写
     */
    @PostMapping("/advanced/chat/stream")
    public Flux<RagChatVO> advancedChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[advancedChatStream] API调用, query={}", request.getQuery());
        return ragService.advancedChatStream(request);
    }

    // ==================== 文档管理 ====================

    /**
     * 获取已加载的文档列表
     */
    @GetMapping("/documents")
    public Result<List<String>> getDocuments() {
        return Result.success(ragService.getDocuments());
    }
}
