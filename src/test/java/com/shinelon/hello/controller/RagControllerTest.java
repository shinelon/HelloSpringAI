package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RagController 集成测试
 *
 * @author shinelon
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDocuments_shouldReturnDocumentList() throws Exception {
        mockMvc.perform(get("/learn/rag/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data", containsInAnyOrder(
                        "公司介绍", "产品说明", "技术架构", "常见问题", "联系方式"
                )));
    }

    @Test
    void simpleChat_withValidRequest_shouldReturnResponse() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("公司主要做什么业务？");

        mockMvc.perform(post("/learn/rag/simple/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", not(emptyString())))
                .andExpect(jsonPath("$.data.createTime", notNullValue()));
    }

    @Test
    void simpleChat_withEmptyQuery_shouldReturnBadRequest() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        mockMvc.perform(post("/learn/rag/simple/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void advancedChat_withValidRequest_shouldReturnResponse() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("我想了解一下你们的技术");

        mockMvc.perform(post("/learn/rag/advanced/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", not(emptyString())));
    }

    @Test
    void advancedChat_withEmptyQuery_shouldReturnBadRequest() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        mockMvc.perform(post("/learn/rag/advanced/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
