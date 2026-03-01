package com.shinelon.hello.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RagChatManager 单元测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
class RagChatManagerTest {

    @Autowired
    private RagChatManager ragChatManager;

    @Test
    void getDocumentSources_shouldReturnFiveDocuments() {
        List<String> sources = ragChatManager.getDocumentSources();
        
        assertNotNull(sources);
        assertEquals(5, sources.size());
        assertTrue(sources.contains("公司介绍"));
        assertTrue(sources.contains("产品说明"));
        assertTrue(sources.contains("技术架构"));
        assertTrue(sources.contains("常见问题"));
        assertTrue(sources.contains("联系方式"));
    }

    @Test
    void simpleChat_withValidQuery_shouldReturnResponse() {
        String response = ragChatManager.simpleChat("公司主要做什么业务？");
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void simpleChat_withEmptyQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.simpleChat("");
        });
    }

    @Test
    void simpleChat_withNullQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.simpleChat(null);
        });
    }

    @Test
    void advancedChat_withValidQuery_shouldReturnResponse() {
        String response = ragChatManager.advancedChat("我想了解你们的技术架构");
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void advancedChat_withEmptyQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.advancedChat("");
        });
    }
}
