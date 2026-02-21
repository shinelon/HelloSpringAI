package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.constants.CommonConstants;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.dao.ChatMessageDao;
import com.shinelon.hello.dao.ChatSessionDao;
import com.shinelon.hello.manager.ZhipuAiManager;
import com.shinelon.hello.model.dto.ChatRequestDTO;
import com.shinelon.hello.model.entity.ChatMessageDO;
import com.shinelon.hello.model.entity.ChatSessionDO;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.model.vo.SessionVO;
import com.shinelon.hello.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 *
 * @author shinelon
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionDao chatSessionDao;
    private final ChatMessageDao chatMessageDao;
    private final ZhipuAiManager zhipuAiManager;

    @Override
    @Transactional
    public MessageVO chat(ChatRequestDTO request) {
        // 参数校验
        validateRequest(request);

        String sessionId = request.getSessionId();
        boolean isNewSession = sessionId == null || sessionId.trim().isEmpty();

        log.info("[chat] 请求开始, sessionId={}, isNewSession={}, content={}",
                sessionId, isNewSession, DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        // 获取或创建会话
        ChatSessionDO session;
        if (isNewSession) {
            session = createNewSession(request.getContent());
            sessionId = session.getSessionId();
            log.debug("[chat] 创建新会话: sessionId={}", sessionId);
        } else {
            session = chatSessionDao.findBySessionId(sessionId)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.NOT_FOUND, "会话不存在"));
            log.debug("[chat] 使用已有会话: sessionId={}", sessionId);
        }

        // 保存用户消息
        saveMessage(sessionId, "user", request.getContent());

        // 获取历史消息并调用AI
        List<Message> messages = buildMessages(sessionId);
        log.debug("[chat] AI调用开始, 消息数量={}", messages.size());

        String aiResponse = zhipuAiManager.syncCallWithHistory(messages);
        log.info("[chat] AI调用成功, sessionId={}, 响应长度={}", sessionId, aiResponse.length());

        // 保存AI回复
        ChatMessageDO assistantMessage = saveMessage(sessionId, "assistant", aiResponse);

        // 更新会话时间
        session.setUpdateTime(LocalDateTime.now());
        chatSessionDao.save(session);

        log.info("[chat] 请求完成, sessionId={}", sessionId);
        return buildMessageVO(sessionId, assistantMessage);
    }

    @Override
    @Transactional
    public Flux<MessageVO> chatStream(ChatRequestDTO request) {
        // 参数校验
        validateRequest(request);

        String inputSessionId = request.getSessionId();
        boolean isNewSession = inputSessionId == null || inputSessionId.trim().isEmpty();

        log.info("[chatStream] 流式调用开始, sessionId={}, isNewSession={}, content={}",
                inputSessionId, isNewSession, DesensitizationUtils.truncateAndMask(request.getContent(), 50));

        // 获取或创建会话
        ChatSessionDO session;
        if (isNewSession) {
            session = createNewSession(request.getContent());
            log.debug("[chatStream] 创建新会话: sessionId={}", session.getSessionId());
        } else {
            session = chatSessionDao.findBySessionId(inputSessionId)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.NOT_FOUND, "会话不存在"));
        }

        final String sessionId = session.getSessionId();

        // 保存用户消息
        saveMessage(sessionId, "user", request.getContent());

        // 获取历史消息
        List<Message> messages = buildMessages(sessionId);
        log.debug("[chatStream] 流式AI调用开始, 消息数量={}", messages.size());

        // 使用 AtomicReference 确保线程安全
        final AtomicReference<StringBuilder> responseAccumulator =
                new AtomicReference<>(new StringBuilder());

        return zhipuAiManager.streamCallWithHistory(messages)
                .map(chunk -> {
                    responseAccumulator.get().append(chunk);
                    return MessageVO.builder()
                            .sessionId(sessionId)
                            .content(chunk)
                            .build();
                })
                .doOnComplete(() -> {
                    // 流式完成后保存完整回复
                    String fullResponse = responseAccumulator.get().toString();
                    saveMessage(sessionId, "assistant", fullResponse);
                    session.setUpdateTime(LocalDateTime.now());
                    chatSessionDao.save(session);
                    log.info("[chatStream] 流式调用完成, sessionId={}, 响应长度={}", sessionId, fullResponse.length());
                })
                .doOnError(e -> log.error("[chatStream] 流式调用错误, sessionId={}, error={}", sessionId, e.getMessage(), e));
    }

    @Override
    @Transactional
    public String createSession() {
        ChatSessionDO session = new ChatSessionDO();
        session.setSessionId(UUID.randomUUID().toString());
        session.setTitle("新会话");
        chatSessionDao.save(session);
        log.info("[createSession] 创建会话成功, sessionId={}", session.getSessionId());
        return session.getSessionId();
    }

    @Override
    public SessionVO getSession(String sessionId) {
        log.debug("[getSession] 查询会话, sessionId={}", sessionId);

        ChatSessionDO session = chatSessionDao.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.NOT_FOUND, "会话不存在"));

        List<ChatMessageDO> messages = chatMessageDao.findBySessionIdOrderByCreateTimeAsc(sessionId);
        List<MessageVO> messageVOs = messages.stream()
                .map(msg -> MessageVO.builder()
                        .sessionId(sessionId)
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .createTime(msg.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        log.info("[getSession] 查询会话成功, sessionId={}, title={}, 消息数量={}",
                sessionId, session.getTitle(), messages.size());

        return SessionVO.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .messages(messageVOs)
                .build();
    }

    @Override
    public List<SessionVO> listSessions(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ChatSessionDO> sessionPage = chatSessionDao.findAllByOrderByUpdateTimeDesc(pageRequest);

        return sessionPage.getContent().stream()
                .map(session -> SessionVO.builder()
                        .sessionId(session.getSessionId())
                        .title(session.getTitle())
                        .createTime(session.getCreateTime())
                        .updateTime(session.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        log.info("[deleteSession] 删除会话开始, sessionId={}", sessionId);

        if (!chatSessionDao.existsBySessionId(sessionId)) {
            throw new BusinessException(ErrorCodeEnum.NOT_FOUND, "会话不存在");
        }

        // 级联删除消息（由外键约束处理）
        chatSessionDao.deleteBySessionId(sessionId);
        log.info("[deleteSession] 删除会话成功, sessionId={}", sessionId);
    }

    /**
     * 创建新会话
     */
    private ChatSessionDO createNewSession(String firstMessage) {
        ChatSessionDO session = new ChatSessionDO();
        session.setSessionId(UUID.randomUUID().toString());
        session.setTitle(generateTitle(firstMessage));
        return chatSessionDao.save(session);
    }

    /**
     * 生成会话标题
     */
    private String generateTitle(String content) {
        if (content == null || content.isEmpty()) {
            return "新会话";
        }
        return content.length() > CommonConstants.TITLE_MAX_LENGTH
                ? content.substring(0, CommonConstants.TITLE_MAX_LENGTH) + "..."
                : content;
    }

    /**
     * 保存消息
     */
    private ChatMessageDO saveMessage(String sessionId, String role, String content) {
        ChatMessageDO message = new ChatMessageDO();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        return chatMessageDao.save(message);
    }

    /**
     * 构建消息历史
     */
    private List<Message> buildMessages(String sessionId) {
        List<ChatMessageDO> history = chatMessageDao.findBySessionIdOrderByCreateTimeAsc(sessionId);
        List<Message> messages = new ArrayList<>();

        for (ChatMessageDO msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(zhipuAiManager.createUserMessage(msg.getContent()));
            } else {
                messages.add(zhipuAiManager.createAssistantMessage(msg.getContent()));
            }
        }

        return messages;
    }

    /**
     * 构建消息VO
     */
    private MessageVO buildMessageVO(String sessionId, ChatMessageDO message) {
        return MessageVO.builder()
                .sessionId(sessionId)
                .role(message.getRole())
                .content(message.getContent())
                .createTime(message.getCreateTime())
                .build();
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(ChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}
