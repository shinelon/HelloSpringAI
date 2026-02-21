package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.model.vo.MessageVO;
import com.shinelon.hello.model.vo.SessionVO;
import com.shinelon.hello.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SessionController 测试类
 *
 * @author shinelon
 */
@WebMvcTest(SessionController.class)
@ActiveProfiles("test")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private String testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = UUID.randomUUID().toString();
    }

    @Nested
    @DisplayName("创建会话测试")
    class CreateSessionTests {

        @Test
        @DisplayName("创建会话 - 应返回200和会话ID")
        void createSession_shouldReturn200() throws Exception {
            // Given
            String newSessionId = UUID.randomUUID().toString();
            when(chatService.createSession()).thenReturn(newSessionId);

            // When & Then
            mockMvc.perform(post("/sessions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(newSessionId));
        }
    }

    @Nested
    @DisplayName("获取会话详情测试")
    class GetSessionTests {

        @Test
        @DisplayName("获取存在的会话 - 应返回200")
        void getSession_existing_shouldReturn200() throws Exception {
            // Given
            SessionVO session = SessionVO.builder()
                    .sessionId(testSessionId)
                    .title("测试会话")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .messages(Collections.emptyList())
                    .build();

            when(chatService.getSession(testSessionId)).thenReturn(session);

            // When & Then
            mockMvc.perform(get("/sessions/{sessionId}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value(testSessionId))
                    .andExpect(jsonPath("$.data.title").value("测试会话"));
        }

        @Test
        @DisplayName("获取不存在的会话 - 应返回404")
        void getSession_notExisting_shouldReturn404() throws Exception {
            // Given - 使用有效的 UUID 格式
            String nonExistingId = UUID.randomUUID().toString();
            when(chatService.getSession(nonExistingId))
                    .thenThrow(new BusinessException(ErrorCodeEnum.NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/sessions/{sessionId}", nonExistingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("获取带历史消息的会话")
        void getSession_withMessages_shouldReturnMessages() throws Exception {
            // Given
            List<MessageVO> messages = Arrays.asList(
                    MessageVO.builder().role("user").content("你好").createTime(LocalDateTime.now()).build(),
                    MessageVO.builder().role("assistant").content("你好！").createTime(LocalDateTime.now()).build()
            );

            SessionVO session = SessionVO.builder()
                    .sessionId(testSessionId)
                    .title("测试会话")
                    .messages(messages)
                    .build();

            when(chatService.getSession(testSessionId)).thenReturn(session);

            // When & Then
            mockMvc.perform(get("/sessions/{sessionId}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.messages").isArray())
                    .andExpect(jsonPath("$.data.messages.length()").value(2));
        }
    }

    @Nested
    @DisplayName("获取会话列表测试")
    class ListSessionsTests {

        @Test
        @DisplayName("获取会话列表 - 应返回200")
        void listSessions_shouldReturn200() throws Exception {
            // Given
            List<SessionVO> sessions = Arrays.asList(
                    SessionVO.builder().sessionId(UUID.randomUUID().toString()).title("会话1").build(),
                    SessionVO.builder().sessionId(UUID.randomUUID().toString()).title("会话2").build()
            );

            when(chatService.listSessions(0, 10)).thenReturn(sessions);

            // When & Then
            mockMvc.perform(get("/sessions")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("空列表 - 应返回空数组")
        void listSessions_empty_shouldReturnEmptyArray() throws Exception {
            // Given
            when(chatService.listSessions(anyInt(), anyInt())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("删除会话测试")
    class DeleteSessionTests {

        @Test
        @DisplayName("删除存在的会话 - 应返回200")
        void deleteSession_existing_shouldReturn200() throws Exception {
            // When & Then
            mockMvc.perform(delete("/sessions/{sessionId}", testSessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除不存在的会话 - 应返回404")
        void deleteSession_notExisting_shouldReturn404() throws Exception {
            // Given - 使用有效的 UUID 格式
            String nonExistingId = UUID.randomUUID().toString();
            doThrow(new BusinessException(ErrorCodeEnum.NOT_FOUND))
                    .when(chatService).deleteSession(nonExistingId);

            // When & Then
            mockMvc.perform(delete("/sessions/{sessionId}", nonExistingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }
}
