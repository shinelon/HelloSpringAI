package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RagService 单元测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
class RagServiceTest {

    @Autowired
    private RagService ragService;

    record TestCase(String name, String query, boolean shouldSucceed) {}

    static Stream<TestCase> queryTestCases() {
        return Stream.of(
                new TestCase("公司业务查询", "公司的核心业务是什么？", true),
                new TestCase("产品查询", "你们有哪些产品？", true),
                new TestCase("技术查询", "技术架构是怎样的？", true),
                new TestCase("联系方式查询", "怎么联系客服？", true),
                new TestCase("常见问题", "如何获取API密钥？", true)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("queryTestCases")
    void simpleChat_withVariousQueries(TestCase tc) {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery(tc.query());

        RagChatVO response = ragService.simpleChat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
        assertNotNull(response.getCreateTime());
    }

    @Test
    void simpleChat_withNullRequest_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragService.simpleChat(null);
        });
    }

    @Test
    void simpleChat_withEmptyQuery_shouldThrowException() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        assertThrows(IllegalArgumentException.class, () -> {
            ragService.simpleChat(request);
        });
    }

    @Test
    void advancedChat_withValidQuery_shouldReturnResponse() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("我想了解一下你们公司");

        RagChatVO response = ragService.advancedChat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void getDocuments_shouldReturnFiveDocuments() {
        List<String> documents = ragService.getDocuments();

        assertNotNull(documents);
        assertEquals(5, documents.size());
    }

    @Test
    void simpleChatStream_shouldReturnFlux() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("介绍一下公司");

        List<RagChatVO> results = ragService.simpleChatStream(request)
                .take(3)
                .collectList()
                .block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void advancedChatStream_shouldReturnFlux() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("技术栈是什么");

        List<RagChatVO> results = ragService.advancedChatStream(request)
                .take(3)
                .collectList()
                .block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
