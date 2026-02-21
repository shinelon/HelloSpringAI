package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.model.dto.ToolChatRequestDTO;
import com.shinelon.hello.model.vo.ToolChatVO;
import com.shinelon.hello.service.ToolChatService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ToolChatController 测试类
 * 使用表格驱动测试风格
 *
 * @author shinelon
 */
@WebMvcTest(ToolChatController.class)
@ActiveProfiles("test")
@DisplayName("ToolChatController 测试")
class ToolChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ToolChatService toolChatService;

    /**
     * 测试用例数据
     */
    record ChatTestCase(
            String name,
            String content,
            List<String> enabledTools,
            int expectedStatus
    ) {}

    static Stream<ChatTestCase> chatTestCases() {
        return Stream.of(
                new ChatTestCase("正常对话-无工具限制", "今天星期几？", null, 200),
                new ChatTestCase("正常对话-启用datetime", "今天星期几？", List.of("datetime"), 200),
                new ChatTestCase("正常对话-启用calculator", "1+1=?", List.of("calculator"), 200),
                new ChatTestCase("正常对话-启用多个工具", "今天几号？1+1=?", List.of("datetime", "calculator"), 200),
                new ChatTestCase("带空格的消息", "  你好  ", null, 200),
                new ChatTestCase("空消息内容", null, null, 400),
                new ChatTestCase("空消息内容-空字符串", "", null, 400),
                new ChatTestCase("消息过长", "a".repeat(4001), null, 400)
        );
    }

    @BeforeEach
    void setUp() {
        ToolChatVO mockResponse = ToolChatVO.builder()
                .content("AI回复内容")
                .createTime(LocalDateTime.now())
                .build();

        when(toolChatService.chat(any())).thenReturn(mockResponse);
        when(toolChatService.chatStream(any())).thenReturn(
                Flux.just(
                        ToolChatVO.builder().content("今").build(),
                        ToolChatVO.builder().content("天").build(),
                        ToolChatVO.builder().content("是").build(),
                        ToolChatVO.builder().content("星").build(),
                        ToolChatVO.builder().content("期").build(),
                        ToolChatVO.builder().content("五").build()
                )
        );
        when(toolChatService.getAvailableTools()).thenReturn(Arrays.asList("datetime", "calculator"));
    }

    @Nested
    @DisplayName("同步对话接口测试")
    class SyncChatTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.controller.ToolChatControllerTest#chatTestCases")
        @DisplayName("同步对话参数验证")
        void chat_parameterValidation(ChatTestCase testCase) throws Exception {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent(testCase.content());
            request.setEnabledTools(testCase.enabledTools());

            // When & Then
            mockMvc.perform(post("/learn/tool/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(testCase.expectedStatus()));

            if (testCase.expectedStatus() == 200) {
                verify(toolChatService).chat(any());
            }
        }

        @Test
        @DisplayName("正常对话 - 应返回正确响应结构")
        void chat_normal_shouldReturnCorrectStructure() throws Exception {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");

            // When & Then
            mockMvc.perform(post("/learn/tool/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("AI回复内容"))
                    .andExpect(jsonPath("$.data.createTime").exists());
        }

        @Test
        @DisplayName("启用指定工具 - 应正确传递参数")
        void chat_withEnabledTools_shouldPassParameters() throws Exception {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");
            request.setEnabledTools(List.of("datetime"));

            // When & Then
            mockMvc.perform(post("/learn/tool/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(toolChatService).chat(any());
        }

        @Test
        @DisplayName("AI服务异常 - 应返回错误响应")
        void chat_serviceException_shouldReturnError() throws Exception {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");

            when(toolChatService.chat(any()))
                    .thenThrow(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE));

            // When & Then
            mockMvc.perform(post("/learn/tool/chat")
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
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");

            // When & Then
            mockMvc.perform(post("/learn/tool/chat/stream")
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
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("");

            // When & Then
            mockMvc.perform(post("/learn/tool/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("流式对话带工具限制 - 应返回SSE")
        void chatStream_withEnabledTools_shouldReturnSSE() throws Exception {
            // Given
            ToolChatRequestDTO request = new ToolChatRequestDTO();
            request.setContent("今天星期几？");
            request.setEnabledTools(List.of("datetime"));

            // When & Then
            mockMvc.perform(post("/learn/tool/chat/stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("获取工具列表接口测试")
    class ListToolsTests {

        @Test
        @DisplayName("获取工具列表 - 应返回正确列表")
        void listTools_shouldReturnToolList() throws Exception {
            // When & Then
            mockMvc.perform(get("/learn/tool/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0]").value("datetime"))
                    .andExpect(jsonPath("$.data[1]").value("calculator"));

            verify(toolChatService).getAvailableTools();
        }

        @Test
        @DisplayName("获取工具列表 - 应返回2个工具")
        void listTools_shouldReturnTwoTools() throws Exception {
            // When & Then
            mockMvc.perform(get("/learn/tool/list")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }
}
