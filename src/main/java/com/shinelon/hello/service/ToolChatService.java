package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.ToolChatRequestDTO;
import com.shinelon.hello.model.vo.ToolChatVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Tool Calling 服务接口
 *
 * @author shinelon
 */
public interface ToolChatService {

    /**
     * 带工具的同步对话
     *
     * @param request 请求参数
     * @return 响应
     */
    ToolChatVO chat(ToolChatRequestDTO request);

    /**
     * 带工具的流式对话
     *
     * @param request 请求参数
     * @return 响应流
     */
    Flux<ToolChatVO> chatStream(ToolChatRequestDTO request);

    /**
     * 获取可用的工具列表
     *
     * @return 工具名称列表
     */
    List<String> getAvailableTools();
}
