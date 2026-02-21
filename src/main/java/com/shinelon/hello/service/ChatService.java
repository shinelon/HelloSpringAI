package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.ChatRequestDTO;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.model.vo.SessionVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 聊天服务接口
 *
 * @author shinelon
 */
public interface ChatService {

    /**
     * 同步对话
     *
     * @param request 对话请求
     * @return 消息视图
     */
    MessageVO chat(ChatRequestDTO request);

    /**
     * 流式对话
     *
     * @param request 对话请求
     * @return 消息流
     */
    Flux<MessageVO> chatStream(ChatRequestDTO request);

    /**
     * 创建新会话
     *
     * @return 会话ID
     */
    String createSession();

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话视图
     */
    SessionVO getSession(String sessionId);

    /**
     * 获取会话列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 会话列表
     */
    List<SessionVO> listSessions(int page, int size);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);
}
