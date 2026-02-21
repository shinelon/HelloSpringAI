package com.shinelon.hello.service;

import com.shinelon.hello.manager.MemoryChatManager;
import com.shinelon.hello.model.dto.MemoryChatRequestDTO;
import com.shinelon.hello.model.vo.MemoryChatVO;
import com.shinelon.hello.service.impl.MemoryChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MemoryChatService 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("MemoryChatService 测试")
class MemoryChatServiceTest {

    private MemoryChatManager memoryChatManager;
    private MemoryChatService memoryChatService;

    @BeforeEach
    void setUp() {
        memoryChatManager = mock(MemoryChatManager.class);
        memoryChatService = new MemoryChatServiceImpl(memoryChatManager);
    }

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String conversationId,
            String content,
            Class<? extends Exception> expectedException,
            String expectedErrorMessage
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                // 正常场景
                new ChatTestCase("正常对话", "conv-001", "你好", null, null),
                new ChatTestCase("长消息", "conv-002", "a".repeat(4000), null, null),
                new ChatTestCase("带空格的消息", "conv-003", "  你好  ", null, null),

                // 异常场景 - 会话ID
                new ChatTestCase("空会话ID-null", null, "你好", IllegalArgumentException.class, "会话ID不能为空"),
                new ChatTestCase("空会话ID-空字符串", "", "你好", IllegalArgumentException.class, "会话ID不能为空"),
                new ChatTestCase("空会话ID-空白字符串", "   ", "你好", IllegalArgumentException.class, "会话ID不能为空"),

                // 异常场景 - 消息内容
                new ChatTestCase("空消息内容-null", "conv-001", null, IllegalArgumentException.class, "消息内容不能为空"),
                new ChatTestCase("空消息内容-空字符串", "conv-001", "", IllegalArgumentException.class, "消息内容不能为空"),
                new ChatTestCase("空消息内容-空白字符串", "conv-001", "   ", IllegalArgumentException.class, "消息内容不能为空")
        );
    }

    static Stream<ChatTestCase> clearMemoryTestCases() {
        return Stream.of(
                // 正常场景
                new ChatTestCase("正常清除", "conv-001", null, null, null),

                // 异常场景
                new ChatTestCase("空会话ID-null", null, null, IllegalArgumentException.class, "会话ID不能为空"),
                new ChatTestCase("空会话ID-空字符串", "", null, IllegalArgumentException.class, "会话ID不能为空"),
                new ChatTestCase("空会话ID-空白字符串", "   ", null, IllegalArgumentException.class, "会话ID不能为空")
        );
    }

    @Nested
    @DisplayName("chat 同步对话测试")
    class ChatTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.service.MemoryChatServiceTest#chatTestCases")
        @DisplayName("同步对话参数验证")
        void chat_parameterValidation(ChatTestCase testCase) {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId(testCase.conversationId());
            request.setContent(testCase.content());

            if (testCase.expectedException() == null) {
                // 正常场景
                when(memoryChatManager.syncCall(anyString(), anyString())).thenReturn("AI回复");

                // When
                MemoryChatVO result = memoryChatService.chat(request);

                // Then
                assertNotNull(result);
                assertEquals(testCase.conversationId(), result.getConversationId());
                assertEquals("AI回复", result.getContent());
                assertNotNull(result.getCreateTime());
                // Service 层不会 trim 内容，直接传递原始值
                verify(memoryChatManager).syncCall(eq(testCase.conversationId()), anyString());
            } else {
                // 异常场景
                Exception exception = assertThrows(testCase.expectedException(), () -> {
                    memoryChatService.chat(request);
                });
                assertTrue(exception.getMessage().contains(testCase.expectedErrorMessage()),
                        () -> "异常消息应包含: " + testCase.expectedErrorMessage() + "，实际: " + exception.getMessage());
                verify(memoryChatManager, never()).syncCall(anyString(), anyString());
            }
        }

        @Test
        @DisplayName("请求为null应抛出异常")
        void chat_nullRequest_shouldThrowException() {
            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                memoryChatService.chat(null);
            });
            assertTrue(exception.getMessage().contains("请求不能为空"));
        }

        @Test
        @DisplayName("正常对话应返回带时间戳的响应")
        void chat_normal_shouldReturnWithTimestamp() {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("你好");
            when(memoryChatManager.syncCall(anyString(), anyString())).thenReturn("AI回复");

            // When
            MemoryChatVO result = memoryChatService.chat(request);

            // Then
            assertNotNull(result.getCreateTime());
            assertTrue(result.getCreateTime().isBefore(LocalDateTime.now().plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("chatStream 流式对话测试")
    class ChatStreamTests {

        @Test
        @DisplayName("流式对话应返回Flux")
        void chatStream_normal_shouldReturnFlux() {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("你好");
            when(memoryChatManager.streamCall(anyString(), anyString()))
                    .thenReturn(Flux.just("你", "好", "！"));

            // When
            Flux<MemoryChatVO> result = memoryChatService.chatStream(request);

            // Then
            StepVerifier.create(result)
                    .assertNext(vo -> {
                        assertEquals("conv-001", vo.getConversationId());
                        assertEquals("你", vo.getContent());
                    })
                    .assertNext(vo -> assertEquals("好", vo.getContent()))
                    .assertNext(vo -> assertEquals("！", vo.getContent()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("请求为null应抛出异常")
        void chatStream_nullRequest_shouldThrowException() {
            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                memoryChatService.chatStream(null);
            });
            assertTrue(exception.getMessage().contains("请求不能为空"));
        }

        @Test
        @DisplayName("空会话ID应抛出异常")
        void chatStream_emptyConversationId_shouldThrowException() {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setContent("你好");

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                memoryChatService.chatStream(request);
            });
            assertTrue(exception.getMessage().contains("会话ID不能为空"));
        }

        @Test
        @DisplayName("空消息内容应抛出异常")
        void chatStream_emptyContent_shouldThrowException() {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                memoryChatService.chatStream(request);
            });
            assertTrue(exception.getMessage().contains("消息内容不能为空"));
        }
    }

    @Nested
    @DisplayName("clearMemory 清除记忆测试")
    class ClearMemoryTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.service.MemoryChatServiceTest#clearMemoryTestCases")
        @DisplayName("清除记忆参数验证")
        void clearMemory_parameterValidation(ChatTestCase testCase) {
            if (testCase.expectedException() == null) {
                // 正常场景
                // When
                memoryChatService.clearMemory(testCase.conversationId());

                // Then
                verify(memoryChatManager).clearMemory(testCase.conversationId());
            } else {
                // 异常场景
                Exception exception = assertThrows(testCase.expectedException(), () -> {
                    memoryChatService.clearMemory(testCase.conversationId());
                });
                assertTrue(exception.getMessage().contains(testCase.expectedErrorMessage()));
                verify(memoryChatManager, never()).clearMemory(anyString());
            }
        }
    }
}
