package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.model.dto.ChatRequestDTO;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.service.ChatService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatController 测试类
 *
 * @author shinelon
 */
@WebMvcTest(ChatController.class)
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String sessionId,
            String content,
            int expectedStatus
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                new ChatTestCase("正常对话", null, "你好", 200),
                new ChatTestCase("带会话ID", "session-123", "继续", 200),
                new ChatTestCase("空内容", null, "", 400),
                new ChatTestCase("null内容", null, null, 400)
        );
    }

    @BeforeEach
    void setUp() {
        MessageVO mockResponse = MessageVO.builder()
                .role("assistant")
                .content("AI回复内容")
                .createTime(LocalDateTime.now())
                .build();

        when(chatService.chat(any())).thenReturn(mockResponse);
        when(chatService.chatStream(any())).thenReturn(
                Flux.just(
                        MessageVO.builder().content("你").build(),
                        MessageVO.builder().content("好").build(),
                        MessageVO.builder().content("！").build()
                )
        );
    }

    @Nested
    @DisplayName("同步对话接口测试")
    class SyncChatTests {

        @Test
        @DisplayName("正常对话 - 应返回200")
        void chat_normal_shouldReturn200() throws Exception {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("你好");

            // When & Then
            mockMvc.perform(post("/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("AI回复内容"));
        }

        @Test
        @DisplayName("带会话ID的对话 - 应返回200")
        void chat_withSessionId_shouldReturn200() throws Exception {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setSessionId("session-123");
            request.setContent("继续");

            // When & Then
            mockMvc.perform(post("/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("无效会话ID - 应返回404")
        void chat_invalidSession_shouldReturn404() throws Exception {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setSessionId("non-existent");
            request.setContent("你好");

            when(chatService.chat(any()))
                    .thenThrow(new BusinessException(ErrorCodeEnum.NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("空内容 - 应返回400")
        void chat_emptyContent_shouldReturn400() throws Exception {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("");

            // When & Then
            mockMvc.perform(post("/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("流式对话接口测试")
    class StreamChatTests {

        @Test
        @DisplayName("流式对话 - 应返回SSE")
        void chatStream_normal_shouldReturnSSE() throws Exception {
            // Given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("你好");

            // When & Then
            mockMvc.perform(post("/chat/stream")
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
            ChatRequestDTO request = new ChatRequestDTO();
            request.setContent("");

            // When & Then
            mockMvc.perform(post("/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
