package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 对话服务接口
 *
 * @author shinelon
 */
public interface RagService {

    /**
     * 简单版 RAG 同步对话
     *
     * @param request 请求
     * @return 响应
     */
    RagChatVO simpleChat(RagChatRequestDTO request);

    /**
     * 简单版 RAG 流式对话
     *
     * @param request 请求
     * @return 响应流
     */
    Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request);

    /**
     * 进阶版 RAG 同步对话
     *
     * @param request 请求
     * @return 响应
     */
    RagChatVO advancedChat(RagChatRequestDTO request);

    /**
     * 进阶版 RAG 流式对话
     *
     * @param request 请求
     * @return 响应流
     */
    Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request);

    /**
     * 获取已加载的文档列表
     *
     * @return 文档来源列表
     */
    List<String> getDocuments();
}
