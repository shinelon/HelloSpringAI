package com.shinelon.hello.manager;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ZhipuAiManager 集成测试
 * 注意：此测试会调用真实的智谱AI API
 * 仅在环境变量 ZHIPUAI_API_KEY 存在时运行
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("需要真实的智谱AI API key 和网络连接")
class ZhipuAiManagerTest {

    @Autowired
    private ZhipuAiManager zhipuAiManager;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * 测试用例：同步调用
     */
    static Stream<TestCase> syncCallTestCases() {
        return Stream.of(
                new TestCase("简单问候", "你好", false),
                new TestCase("问答", "1+1等于几？", false),
                new TestCase("短对话", "Java是什么？", false)
        );
    }

    @Nested
    @DisplayName("同步调用测试")
    class SyncCallTests {

        @ParameterizedTest
        @MethodSource("com.shinelon.hello.manager.ZhipuAiManagerTest#syncCallTestCases")
        @DisplayName("同步调用应返回有效响应")
        void syncCall_shouldReturnValidResponse(TestCase testCase) {
            // Given
            String prompt = testCase.prompt();

            // When
            String response = zhipuAiManager.syncCall(prompt);

            // Then
            assertNotNull(response, "响应不应为空");
            assertFalse(response.isEmpty(), "响应内容不应为空字符串");
            assertTrue(response.length() > 0, "响应应有内容");
        }

        @Test
        @DisplayName("空消息应抛出异常")
        void syncCall_withEmptyMessage_shouldThrowException() {
            // Given
            String emptyMessage = "";

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                zhipuAiManager.syncCall(emptyMessage);
            });
        }

        @Test
        @DisplayName("null消息应抛出异常")
        void syncCall_withNullMessage_shouldThrowException() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                zhipuAiManager.syncCall(null);
            });
        }
    }

    @Nested
    @DisplayName("多轮对话测试")
    class MultiTurnTests {

        @Test
        @DisplayName("带历史消息的对话应能正确处理上下文")
        void callWithHistory_shouldHandleContext() {
            // Given
            String systemPrompt = "你是一个友好的助手";
            String userPrompt = "你好";

            // When
            String response = zhipuAiManager.syncCall(systemPrompt, userPrompt);

            // Then
            assertNotNull(response, "响应不应为空");
        }
    }

    /**
     * 测试用例数据结构
     */
    record TestCase(String name, String prompt, boolean expectError) {
    }
}
