package com.shinelon.hello.dao;

import com.shinelon.hello.model.entity.ChatSessionDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatSessionDao 测试类
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DataJpaTest
@ActiveProfiles("test")
class ChatSessionDaoTest {

    @Autowired
    private ChatSessionDao chatSessionDao;

    /**
     * 测试用例数据
     */
    record SessionTestCase(
            String name,
            String sessionId,
            String title,
            boolean expectSuccess
    ) {}

    static Stream<SessionTestCase> createSessionTestCases() {
        return Stream.of(
                new SessionTestCase("正常创建", UUID.randomUUID().toString(), "测试会话", true),
                new SessionTestCase("空标题", UUID.randomUUID().toString(), "", true),
                new SessionTestCase("长标题", UUID.randomUUID().toString(), "a".repeat(100), true)
        );
    }

    static Stream<SessionTestCase> findBySessionIdTestCases() {
        String existingId = UUID.randomUUID().toString();
        return Stream.of(
                new SessionTestCase("存在的会话", existingId, "测试", true),
                new SessionTestCase("不存在的会话", "non-existent-id", null, false)
        );
    }

    @BeforeEach
    void setUp() {
        chatSessionDao.deleteAll();
    }

    @Nested
    @DisplayName("CRUD 测试")
    class CrudTests {

        @ParameterizedTest
        @MethodSource("com.shinelon.hello.dao.ChatSessionDaoTest#createSessionTestCases")
        @DisplayName("创建会话")
        void create_shouldSucceed(SessionTestCase testCase) {
            // Given
            ChatSessionDO session = new ChatSessionDO();
            session.setSessionId(testCase.sessionId());
            session.setTitle(testCase.title());

            // When
            ChatSessionDO saved = chatSessionDao.save(session);

            // Then
            assertNotNull(saved.getId());
            assertEquals(testCase.sessionId(), saved.getSessionId());
            assertEquals(testCase.title(), saved.getTitle());
            assertNotNull(saved.getCreateTime());
            assertNotNull(saved.getUpdateTime());
        }

        @Test
        @DisplayName("更新会话标题")
        void update_shouldSucceed() {
            // Given
            ChatSessionDO session = createAndSaveSession("原标题");

            // When
            session.setTitle("新标题");
            ChatSessionDO updated = chatSessionDao.save(session);

            // Then
            assertEquals("新标题", updated.getTitle());
        }

        @Test
        @DisplayName("删除会话")
        void delete_shouldSucceed() {
            // Given
            ChatSessionDO session = createAndSaveSession("测试");

            // When
            chatSessionDao.delete(session);

            // Then
            assertFalse(chatSessionDao.existsById(session.getId()));
        }
    }

    @Nested
    @DisplayName("自定义查询测试")
    class CustomQueryTests {

        @Test
        @DisplayName("根据sessionId查询 - 存在")
        void findBySessionId_existing_shouldReturn() {
            // Given
            ChatSessionDO session = createAndSaveSession("测试");

            // When
            Optional<ChatSessionDO> found = chatSessionDao.findBySessionId(session.getSessionId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(session.getSessionId(), found.get().getSessionId());
        }

        @Test
        @DisplayName("根据sessionId查询 - 不存在")
        void findBySessionId_notExisting_shouldReturnEmpty() {
            // When
            Optional<ChatSessionDO> found = chatSessionDao.findBySessionId("non-existent");

            // Then
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("检查sessionId是否存在")
        void existsBySessionId_shouldReturnCorrect() {
            // Given
            ChatSessionDO session = createAndSaveSession("测试");

            // When & Then
            assertTrue(chatSessionDao.existsBySessionId(session.getSessionId()));
            assertFalse(chatSessionDao.existsBySessionId("non-existent"));
        }

        @Test
        @DisplayName("根据sessionId删除")
        void deleteBySessionId_shouldDelete() {
            // Given
            ChatSessionDO session = createAndSaveSession("测试");
            String sessionId = session.getSessionId();

            // When
            chatSessionDao.deleteBySessionId(sessionId);

            // Then
            assertFalse(chatSessionDao.existsBySessionId(sessionId));
        }

        @Test
        @DisplayName("分页查询 - 按更新时间倒序")
        void findAllByOrderByUpdateTimeDesc_shouldReturnPaged() {
            // Given
            for (int i = 0; i < 15; i++) {
                createAndSaveSession("会话" + i);
            }

            // When
            Page<ChatSessionDO> page1 = chatSessionDao.findAllByOrderByUpdateTimeDesc(PageRequest.of(0, 10));
            Page<ChatSessionDO> page2 = chatSessionDao.findAllByOrderByUpdateTimeDesc(PageRequest.of(1, 10));

            // Then
            assertEquals(10, page1.getContent().size());
            assertEquals(5, page2.getContent().size());
            assertEquals(15, page1.getTotalElements());
            assertTrue(page1.hasNext());
            assertFalse(page2.hasNext());
        }
    }

    /**
     * 创建并保存测试会话
     */
    private ChatSessionDO createAndSaveSession(String title) {
        ChatSessionDO session = new ChatSessionDO();
        session.setSessionId(UUID.randomUUID().toString());
        session.setTitle(title);
        return chatSessionDao.save(session);
    }
}
