package com.shinelon.hello.service;

import com.shinelon.hello.manager.ToolChatManager;
import com.shinelon.hello.model.dto.ToolChatRequestDTO;
import com.shinelon.hello.model.vo.ToolChatVO;
import com.shinelon.hello.service.impl.ToolChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * ToolChatService 单元测试
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@DisplayName("ToolChatService 测试")
class ToolChatServiceTest {

    private ToolChatManager toolChatManager;
    private ToolChatService toolChatService;

    @BeforeEach
    void setUp() {
        toolChatManager = mock(ToolChatManager.class);
        toolChatService = new ToolChatServiceImpl(toolChatManager);
    }

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String content,
            List<String> enabledTools,
            Class<? extends Exception> expectedException,
            String expectedErrorMessage
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                // 正常场景
                new ChatTestCase("正常对话-无工具限制", "今天星期几？", null, null, null),
                new ChatTestCase("正常对话-启用datetime", "今天星期几？", List.of("datetime"), null, null),
                new ChatTestCase("正常对话-启用calculator", "1+1=?", List.of("calculator"), null, null),
                new ChatTestCase("正常对话-启用多个工具", "今天几号？1+1=?", List.of("datetime", "calculator"), null, null),
                new ChatTestCase("正常对话-启用不存在的工具", "你好", List.of("nonexistent"), null, null),
                new ChatTestCase("正常对话-空工具列表", "你好", Collections.emptyList(), null, null),
                new ChatTestCase("长消息", "a".repeat(4000), null, null, null),
                new ChatTestCase("带空格的消息", "  你好  ", null, null, null),

                // 异常场景
                new ChatTestCase("空消息内容-null", null, null, IllegalArgumentException.class, "消息内容不能为空"),
                new ChatTestCase("空消息内容-空字符串", "", null, IllegalArgumentException.class, "消息内容不能为空"),
                new ChatTestCase("空消息内容-空白字符串", "   ", null, IllegalArgumentException.class, "消息内容不能为空")
        );
    }

    @Nested
    @DisplayName("chat 同步对话测试")
    class ChatTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.service.ToolChatServiceTest#chatTestCases")
        @DisplayName("同步对话参数验证")
        void chat_parameterValidation(ChatTestCase testCase) {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent(testCase.content());
            request.setEnabledTools(testCase.enabledTools());

            if (testCase.expectedException() == null) {
                // 正常场景
                when(toolChatManager.syncCall(anyString(), any())).thenReturn("AI回复");

                // When
                ToolChatVO result = toolChatService.chat(request);

                // Then
                assertNotNull(result);
                assertEquals("AI回复", result.getContent());
                assertNotNull(result.getCreateTime());
            } else {
                // 异常场景
                Exception exception = assertThrows(testCase.expectedException(), () -> {
                    toolChatService.chat(request);
                });
                assertTrue(exception.getMessage().contains(testCase.expectedErrorMessage()),
                        () -> "异常消息应包含: " + testCase.expectedErrorMessage() + "，实际: " + exception.getMessage());
                verify(toolChatManager, never()).syncCall(anyString(), any());
            }
        }

        @Test
        @DisplayName("请求为null应抛出异常")
        void chat_nullRequest_shouldThrowException() {
            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                toolChatService.chat(null);
            });
            assertTrue(exception.getMessage().contains("请求不能为空"));
        }

        @Test
        @DisplayName("正常对话应正确传递enabledTools")
        void chat_shouldPassEnabledTools() {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");
            request.setEnabledTools(List.of("datetime"));
            when(toolChatManager.syncCall(anyString(), any())).thenReturn("今天是星期五");

            // When
            toolChatService.chat(request);

            // Then
            verify(toolChatManager).syncCall(eq("今天星期几？"), eq(List.of("datetime")));
        }

        @Test
        @DisplayName("enabledTools为null时使用全部工具")
        void chat_nullEnabledTools_useAllTools() {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");
            request.setEnabledTools(null);
            when(toolChatManager.syncCall(anyString(), isNull())).thenReturn("AI回复");

            // When
            toolChatService.chat(request);

            // Then
            verify(toolChatManager).syncCall(eq("今天星期几？"), isNull());
        }

        @Test
        @DisplayName("正常对话应返回带时间戳的响应")
        void chat_normal_shouldReturnWithTimestamp() {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("你好");
            when(toolChatManager.syncCall(anyString(), any())).thenReturn("AI回复");

            // When
            ToolChatVO result = toolChatService.chat(request);

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
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");
            when(toolChatManager.streamCall(anyString(), any()))
                    .thenReturn(Flux.just("今天", "是", "星期五"));

            // When
            Flux<ToolChatVO> result = toolChatService.chatStream(request);

            // Then
            StepVerifier.create(result)
                    .assertNext(vo -> assertEquals("今天", vo.getContent()))
                    .assertNext(vo -> assertEquals("是", vo.getContent()))
                    .assertNext(vo -> assertEquals("星期五", vo.getContent()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("请求为null应抛出异常")
        void chatStream_nullRequest_shouldThrowException() {
            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                toolChatService.chatStream(null);
            });
            assertTrue(exception.getMessage().contains("请求不能为空"));
        }

        @Test
        @DisplayName("空消息内容应抛出异常")
        void chatStream_emptyContent_shouldThrowException() {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();

            // When & Then
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                toolChatService.chatStream(request);
            });
            assertTrue(exception.getMessage().contains("消息内容不能为空"));
        }

        @Test
        @DisplayName("流式对话应正确传递enabledTools")
        void chatStream_shouldPassEnabledTools() {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("1+1=?");
            request.setEnabledTools(List.of("calculator"));
            when(toolChatManager.streamCall(anyString(), any()))
                    .thenReturn(Flux.just("2"));

            // When
            toolChatService.chatStream(request).blockLast();

            // Then
            verify(toolChatManager).streamCall(eq("1+1=?"), eq(List.of("calculator")));
        }
    }

    @Nested
    @DisplayName("getAvailableTools 获取工具列表测试")
    class GetAvailableToolsTests {

        @Test
        @DisplayName("应返回可用工具列表")
        void getAvailableTools_shouldReturnToolList() {
            // Given
            List<String> expectedTools = Arrays.asList("datetime", "calculator");
            when(toolChatManager.getAvailableTools()).thenReturn(expectedTools);

            // When
            List<String> result = toolChatService.getAvailableTools();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("datetime"));
            assertTrue(result.contains("calculator"));
            verify(toolChatManager).getAvailableTools();
        }

        @Test
        @DisplayName("应返回工具列表")
        void getAvailableTools_shouldReturnList() {
            // Given
            List<String> expectedTools = Arrays.asList("datetime", "calculator");
            when(toolChatManager.getAvailableTools()).thenReturn(expectedTools);

            // When
            List<String> result = toolChatService.getAvailableTools();

            // Then
            assertNotNull(result);
            assertEquals(expectedTools, result);
        }
    }
}
