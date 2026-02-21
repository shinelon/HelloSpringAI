package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.MemoryChatRequestDTO;
import com.shinelon.hello.model.vo.MemoryChatVO;
import reactor.core.publisher.Flux;

/**
 * Chat Memory 服务接口
 *
 * @author shinelon
 */
public interface MemoryChatService {

    /**
     * 带记忆的同步对话
     *
     * @param request 请求参数
     * @return 响应
     */
    MemoryChatVO chat(MemoryChatRequestDTO request);

    /**
     * 带记忆的流式对话
     *
     * @param request 请求参数
     * @return 响应流
     */
    Flux<MemoryChatVO> chatStream(MemoryChatRequestDTO request);

    /**
     * 清除会话记忆
     *
     * @param conversationId 会话ID
     */
    void clearMemory(String conversationId);
}
