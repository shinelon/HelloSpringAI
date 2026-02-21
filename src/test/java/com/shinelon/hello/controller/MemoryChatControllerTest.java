package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.model.dto.MemoryChatRequestDTO;
import com.shinelon.hello.model.vo.MemoryChatVO;
import com.shinelon.hello.service.MemoryChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemoryChatController 测试类
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@WebMvcTest(MemoryChatController.class)
@ActiveProfiles("test")
@DisplayName("MemoryChatController 测试")
class MemoryChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemoryChatService memoryChatService;

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String conversationId,
            String content,
            int expectedStatus
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                new ChatTestCase("正常对话", "conv-001", "你好", 200),
                new ChatTestCase("带空格的消息", "conv-001", "  你好  ", 200),
                new ChatTestCase("空会话ID", null, "你好", 400),
                new ChatTestCase("空会话ID-空字符串", "", "你好", 400),
                new ChatTestCase("空消息内容", "conv-001", null, 400),
                new ChatTestCase("空消息内容-空字符串", "conv-001", "", 400),
                new ChatTestCase("消息过长", "conv-001", "a".repeat(4001), 400)
        );
    }

    static Stream<ChatTestCase> clearMemoryTestCases() {
        return Stream.of(
                new ChatTestCase("正常清除", "conv-001", null, 200)
        );
    }

    @BeforeEach
    void setUp() {
        MemoryChatVO mockResponse = MemoryChatVO.builder()
                .conversationId("conv-001")
                .content("AI回复内容")
                .createTime(LocalDateTime.now())
                .build();

        when(memoryChatService.chat(any())).thenReturn(mockResponse);
        when(memoryChatService.chatStream(any())).thenReturn(
                Flux.just(
                        MemoryChatVO.builder().conversationId("conv-001").content("你").build(),
                        MemoryChatVO.builder().conversationId("conv-001").content("好").build(),
                        MemoryChatVO.builder().conversationId("conv-001").content("！").build()
                )
        );
        doNothing().when(memoryChatService).clearMemory(anyString());
    }

    @Nested
    @DisplayName("同步对话接口测试")
    class SyncChatTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.controller.MemoryChatControllerTest#chatTestCases")
        @DisplayName("同步对话参数验证")
        void chat_parameterValidation(ChatTestCase testCase) throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId(testCase.conversationId());
            request.setContent(testCase.content());

            // When & Then
            mockMvc.perform(post("/learn/memory/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(testCase.expectedStatus()));

            if (testCase.expectedStatus() == 200) {
                verify(memoryChatService).chat(any());
            }
        }

        @Test
        @DisplayName("正常对话 - 应返回正确响应结构")
        void chat_normal_shouldReturnCorrectStructure() throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("你好");

            // When & Then
            mockMvc.perform(post("/learn/memory/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.conversationId").value("conv-001"))
                    .andExpect(jsonPath("$.data.content").value("AI回复内容"))
                    .andExpect(jsonPath("$.data.createTime").exists());
        }

        @Test
        @DisplayName("AI服务异常 - 应返回错误响应")
        void chat_serviceException_shouldReturnError() throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("你好");

            when(memoryChatService.chat(any()))
                    .thenThrow(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE));

            // When & Then
            mockMvc.perform(post("/learn/memory/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(503)); // AI_SERVICE_UNAVAILABLE 对应 503
        }
    }

    @Nested
    @DisplayName("流式对话接口测试")
    class StreamChatTests {

        @Test
        @DisplayName("流式对话 - 应返回SSE")
        void chatStream_normal_shouldReturnSSE() throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("你好");

            // When & Then
            mockMvc.perform(post("/learn/memory/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(request().asyncStarted());
        }

        @Test
        @DisplayName("流式对话空内容 - 应返回400")
        void chatStream_emptyContent_shouldReturn400() throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setConversationId("conv-001");
            request.setContent("");

            // When & Then
            mockMvc.perform(post("/learn/memory/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("流式对话空会话ID - 应返回400")
        void chatStream_emptyConversationId_shouldReturn400() throws Exception {
            // Given
            MemoryChatRequestDTO request = new MemoryChatRequestDTO();
            request.setContent("你好");

            // When & Then
            mockMvc.perform(post("/learn/memory/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("清除记忆接口测试")
    class ClearMemoryTests {

        @Test
        @DisplayName("正常清除 - 应调用service")
        void clearMemory_normal_shouldCallService() throws Exception {
            // When & Then
            mockMvc.perform(delete("/learn/memory/conv-001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(memoryChatService).clearMemory("conv-001");
        }

        @Test
        @DisplayName("清除记忆异常 - 应返回错误")
        void clearMemory_serviceException_shouldReturnError() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("会话ID不能为空"))
                    .when(memoryChatService).clearMemory(anyString());

            // When & Then - IllegalArgumentException 会被全局异常处理器转换为 400 状态
            mockMvc.perform(delete("/learn/memory/conv-001")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
