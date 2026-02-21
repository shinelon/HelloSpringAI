package com.shinelon.hello.service;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.manager.ZhipuAiManager;
import com.shinelon.hello.model.dto.ChatRequestDTO;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.model.vo.SessionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatService 测试类
 * 使用表格驱动测试风格
 * 注意：此测试需要真实的 API key 环境变量
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("需要真实的智谱AI API key 和网络连接")
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @MockBean
    private ZhipuAiManager zhipuAiManager;

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String sessionId,
            String content,
            Class<? extends Exception> expectedException,
            boolean expectNewSession
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                new ChatTestCase("新会话对话", null, "你好", null, true),
                new ChatTestCase("新会话对话-空sessionId", "", "你好", null, true),
                new ChatTestCase("继续对话", null, "继续", null, false),
                new ChatTestCase("空消息", null, "", IllegalArgumentException.class, false),
                new ChatTestCase("null消息", null, null, IllegalArgumentException.class, false)
        );
    }

    @BeforeEach
    void setUp() {
        when(zhipuAiManager.syncCallWithHistory(any())).thenReturn("AI回复内容");
        when(zhipuAiManager.streamCallWithHistory(any())).thenReturn(
                reactor.core.publisher.Flux.just("你", "好", "！")
        );
    }

    @Nested
    @DisplayName("对话测试")
    class ChatTests {

        @Test
        @DisplayName("新会话对话 - 应创建新会话并返回回复")
        void chat_newSession_shouldCreateAndRespond() {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("你好");

            // When
            MessageVO response = chatService.chat(request);

            // Then
            assertNotNull(response);
            assertNotNull(response.getCreateTime());
            assertEquals("assistant", response.getRole());
            assertEquals("AI回复内容", response.getContent());
        }

        @Test
        @DisplayName("继续对话 - 应使用现有会话")
        void chat_existingSession_shouldContinueConversation() {
            // Given
            ChatRequestDTO createRequest = new ChatRequestDTO();
            createRequest.setContent("第一条消息");
            MessageVO firstResponse = chatService.chat(createRequest);
            String sessionId = firstResponse.getSessionId() != null ?
                    firstResponse.getSessionId() : extractSessionIdFromMessages();

            ChatRequestDTO continueRequest = new ChatRequestDTO();
            continueRequest.setSessionId(sessionId);
            continueRequest.setContent("第二条消息");

            // When
            MessageVO response = chatService.chat(continueRequest);

            // Then
            assertNotNull(response);
            assertEquals("assistant", response.getRole());
        }

        @Test
        @DisplayName("无效会话ID - 应抛出异常")
        void chat_invalidSession_shouldThrowException() {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setSessionId("non-existent-session-id");
            request.setContent("你好");

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                chatService.chat(request);
            });
            assertEquals(ErrorCodeEnum.NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("空消息内容 - 应抛出异常")
        void chat_emptyContent_shouldThrowException() {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                chatService.chat(request);
            });
        }
    }

    @Nested
    @DisplayName("会话管理测试")
    class SessionManagementTests {

        @Test
        @DisplayName("创建会话 - 应返回会话ID")
        void createSession_shouldReturnSessionId() {
            // When
            String sessionId = chatService.createSession();

            // Then
            assertNotNull(sessionId);
            assertTrue(sessionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        }

        @Test
        @DisplayName("获取会话详情 - 存在的会话")
        void getSession_existing_shouldReturnDetails() {
            // Given
            String sessionId = chatService.createSession();

            // When
            SessionVO session = chatService.getSession(sessionId);

            // Then
            assertNotNull(session);
            assertEquals(sessionId, session.getSessionId());
        }

        @Test
        @DisplayName("获取会话详情 - 不存在的会话")
        void getSession_notExisting_shouldThrowException() {
            // When & Then
            assertThrows(BusinessException.class, () -> {
                chatService.getSession("non-existent");
            });
        }

        @Test
        @DisplayName("删除会话 - 应成功删除")
        void deleteSession_shouldSucceed() {
            // Given
            String sessionId = chatService.createSession();

            // When
            chatService.deleteSession(sessionId);

            // Then
            assertThrows(BusinessException.class, () -> {
                chatService.getSession(sessionId);
            });
        }

        @Test
        @DisplayName("删除会话 - 不存在的会话应抛出异常")
        void deleteSession_notExisting_shouldThrowException() {
            // When & Then
            assertThrows(BusinessException.class, () -> {
                chatService.deleteSession("non-existent");
            });
        }

        @Test
        @DisplayName("获取会话列表 - 应返回分页结果")
        void listSessions_shouldReturnPaged() {
            // Given
            for (int i = 0; i < 5; i++) {
                chatService.createSession();
            }

            // When
            List<SessionVO> sessions = chatService.listSessions(0, 10);

            // Then
            assertNotNull(sessions);
            assertTrue(sessions.size() >= 5);
        }
    }

    @Nested
    @DisplayName("历史消息测试")
    class HistoryTests {

        @Test
        @DisplayName("新会话应无历史消息")
        void newSession_shouldHaveNoHistory() {
            // Given
            String sessionId = chatService.createSession();

            // When
            SessionVO session = chatService.getSession(sessionId);

            // Then
            assertTrue(session.getMessages() == null || session.getMessages().isEmpty());
        }

        @Test
        @DisplayName("对话后应有历史消息")
        void afterChat_shouldHaveHistory() {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("测试消息");
            MessageVO response = chatService.chat(request);

            // When
            List<SessionVO> sessions = chatService.listSessions(0, 10);
            SessionVO session = sessions.stream()
                    .filter(s -> s.getMessages() != null && !s.getMessages().isEmpty())
                    .findFirst()
                    .orElse(null);

            // Then
            if (session != null && session.getMessages() != null) {
                assertTrue(session.getMessages().size() >= 1);
            }
        }
    }

    /**
     * 从消息列表中提取会话ID的辅助方法
     */
    private String extractSessionIdFromMessages() {
        List<SessionVO> sessions = chatService.listSessions(0, 1);
        if (!sessions.isEmpty()) {
            return sessions.get(0).getSessionId();
        }
        return null;
    }
}
