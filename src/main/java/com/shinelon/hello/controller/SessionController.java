package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.model.vo.SessionVO;
import com.shinelon.hello.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ChatService chatService;

    /**
     * 创建新会话
     *
     * @return 会话ID
     */
    @PostMapping
    public Result<Map<String, String>> createSession() {
        log.info("Creating new session");
        String sessionId = chatService.createSession();

        Map<String, String> data = new HashMap<>();
        data.put("sessionId", sessionId);

        return Result.success(data);
    }

    /**
     * 获取会话列表
     *
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 会话列表
     */
    @GetMapping
    public Result<List<SessionVO>> listSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing sessions: page={}, size={}", page, size);

        // 转换为0-based页码
        List<SessionVO> sessions = chatService.listSessions(page - 1, size);
        return Result.success(sessions);
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情（含历史消息）
     */
    @GetMapping("/{sessionId}")
    public Result<SessionVO> getSession(@PathVariable String sessionId) {
        log.info("Getting session: {}", sessionId);

        SessionVO session = chatService.getSession(sessionId);
        return Result.success(session);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        log.info("Deleting session: {}", sessionId);

        chatService.deleteSession(sessionId);
        return Result.success("删除成功", null);
    }
}
