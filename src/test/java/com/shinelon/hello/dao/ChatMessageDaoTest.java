package com.shinelon.hello.dao;

import com.shinelon.hello.model.entity.ChatMessageDO;
import com.shinelon.hello.model.entity.ChatSessionDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatMessageDao 测试类
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DataJpaTest
@ActiveProfiles("test")
class ChatMessageDaoTest {

    @Autowired
    private ChatSessionDao chatSessionDao;

    @Autowired
    private ChatMessageDao chatMessageDao;

    private String testSessionId;

    /**
     * 测试用例数据
     */
    record MessageTestCase(
            String name,
            String role,
            String content,
            boolean expectSuccess
    ) {}

    static Stream<MessageTestCase> createMessageTestCases() {
        return Stream.of(
                new MessageTestCase("用户消息", "user", "你好", true),
                new MessageTestCase("助手消息", "assistant", "你好！有什么可以帮助你的？", true),
                new MessageTestCase("长消息", "user", "a".repeat(1000), true),
                new MessageTestCase("空内容", "user", "", true)
        );
    }

    @BeforeEach
    void setUp() {
        chatMessageDao.deleteAll();
        chatSessionDao.deleteAll();

        // 创建测试会话
        ChatSessionDO session = new ChatSessionDO();
        session.setSessionId(UUID.randomUUID().toString());
        session.setTitle("测试会话");
        chatSessionDao.save(session);
        testSessionId = session.getSessionId();
    }

    @Nested
    @DisplayName("CRUD 测试")
    class CrudTests {

        @ParameterizedTest
        @MethodSource("com.shinelon.hello.dao.ChatMessageDaoTest#createMessageTestCases")
        @DisplayName("创建消息")
        void create_shouldSucceed(MessageTestCase testCase) {
            // Given
            ChatMessageDO message = new ChatMessageDO();
            message.setSessionId(testSessionId);
            message.setRole(testCase.role());
            message.setContent(testCase.content());

            // When
            ChatMessageDO saved = chatMessageDao.save(message);

            // Then
            assertNotNull(saved.getId());
            assertEquals(testSessionId, saved.getSessionId());
            assertEquals(testCase.role(), saved.getRole());
            assertEquals(testCase.content(), saved.getContent());
            assertNotNull(saved.getCreateTime());
        }

        @Test
        @DisplayName("删除消息")
        void delete_shouldSucceed() {
            // Given
            ChatMessageDO message = createAndSaveMessage("user", "测试");

            // When
            chatMessageDao.delete(message);

            // Then
            assertFalse(chatMessageDao.existsById(message.getId()));
        }
    }

    @Nested
    @DisplayName("按会话查询测试")
    class QueryBySessionTests {

        @Test
        @DisplayName("根据会话ID查询消息 - 按时间升序")
        void findBySessionIdOrderByCreateTimeAsc_shouldReturnOrdered() {
            // Given
            createAndSaveMessage("user", "第一条");
            createAndSaveMessage("assistant", "第二条");
            createAndSaveMessage("user", "第三条");

            // When
            List<ChatMessageDO> messages = chatMessageDao.findBySessionIdOrderByCreateTimeAsc(testSessionId);

            // Then
            assertEquals(3, messages.size());
            assertEquals("第一条", messages.get(0).getContent());
            assertEquals("第二条", messages.get(1).getContent());
            assertEquals("第三条", messages.get(2).getContent());
        }

        @Test
        @DisplayName("根据会话ID查询消息 - 无消息时返回空列表")
        void findBySessionId_noMessages_shouldReturnEmpty() {
            // When
            List<ChatMessageDO> messages = chatMessageDao.findBySessionIdOrderByCreateTimeAsc("non-existent");

            // Then
            assertTrue(messages.isEmpty());
        }

        @Test
        @DisplayName("统计会话消息数量")
        void countBySessionId_shouldReturnCorrect() {
            // Given
            createAndSaveMessage("user", "消息1");
            createAndSaveMessage("assistant", "消息2");
            createAndSaveMessage("user", "消息3");

            // When
            long count = chatMessageDao.countBySessionId(testSessionId);

            // Then
            assertEquals(3, count);
        }

        @Test
        @DisplayName("根据会话ID删除所有消息")
        void deleteBySessionId_shouldDeleteAll() {
            // Given
            createAndSaveMessage("user", "消息1");
            createAndSaveMessage("assistant", "消息2");

            // When
            chatMessageDao.deleteBySessionId(testSessionId);

            // Then
            assertEquals(0, chatMessageDao.countBySessionId(testSessionId));
        }
    }

    @Nested
    @DisplayName("多会话测试")
    class MultiSessionTests {

        @Test
        @DisplayName("不同会话的消息应正确隔离")
        void differentSessions_shouldBeIsolated() {
            // Given
            String anotherSessionId = UUID.randomUUID().toString();
            ChatSessionDO anotherSession = new ChatSessionDO();
            anotherSession.setSessionId(anotherSessionId);
            anotherSession.setTitle("另一个会话");
            chatSessionDao.save(anotherSession);

            createAndSaveMessage("user", "会话1的消息");
            createAndSaveMessage(testSessionId, "assistant", "会话1的回复");

            ChatMessageDO msg = new ChatMessageDO();
            msg.setSessionId(anotherSessionId);
            msg.setRole("user");
            msg.setContent("会话2的消息");
            chatMessageDao.save(msg);

            // When
            List<ChatMessageDO> session1Messages = chatMessageDao.findBySessionIdOrderByCreateTimeAsc(testSessionId);
            List<ChatMessageDO> session2Messages = chatMessageDao.findBySessionIdOrderByCreateTimeAsc(anotherSessionId);

            // Then
            assertEquals(2, session1Messages.size());
            assertEquals(1, session2Messages.size());
        }
    }

    /**
     * 创建并保存测试消息（使用测试会话ID）
     */
    private ChatMessageDO createAndSaveMessage(String role, String content) {
        return createAndSaveMessage(testSessionId, role, content);
    }

    /**
     * 创建并保存测试消息
     */
    private ChatMessageDO createAndSaveMessage(String sessionId, String role, String content) {
        ChatMessageDO message = new ChatMessageDO();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        return chatMessageDao.save(message);
    }
}
