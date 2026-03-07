package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RBAC控制器测试
 *
 * @author shinelon
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void chat_greeting_shouldReturn200() throws Exception {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("你好")
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.createTime").exists());
    }

    @Test
    void chat_normalQuery_shouldReturn200() throws Exception {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("查询用户信息")
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    void chat_emptyContent_shouldReturn400() throws Exception {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("")
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_nullContent_shouldReturn400() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_tooLongContent_shouldReturn400() throws Exception {
        String longContent = "a".repeat(5000);
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content(longContent)
                .build();

        mockMvc.perform(post("/rbac/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
