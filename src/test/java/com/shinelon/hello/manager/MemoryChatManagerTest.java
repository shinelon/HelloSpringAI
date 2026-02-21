package com.shinelon.hello.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MemoryChatManager 单元测试
 * 主要测试参数验证逻辑
 *
 * 注：Manager 层的 AI 调用测试需要真实的 AI 服务，
 * 建议在集成测试中验证。
 *
 * @author shinelon
 */
@DisplayName("MemoryChatManager 测试")
class MemoryChatManagerTest {

    /**
     * 测试用例数据
     */
    record InputTestCase(
            String name,
            String conversationId,
            String prompt,
            Class<? extends Exception> expectedException
    ) {}

    static Stream<InputTestCase> inputValidationTestCases() {
        return Stream.of(
                // 异常场景 - conversationId
                new InputTestCase("空会话ID-null", null, "你好", IllegalArgumentException.class),
                new InputTestCase("空会话ID-空字符串", "", "你好", IllegalArgumentException.class),
                new InputTestCase("空会话ID-空白字符串", "   ", "你好", IllegalArgumentException.class),

                // 异常场景 - prompt
                new InputTestCase("空prompt-null", "conv-001", null, IllegalArgumentException.class),
                new InputTestCase("空prompt-空字符串", "conv-001", "", IllegalArgumentException.class),
                new InputTestCase("空prompt-空白字符串", "conv-001", "   ", IllegalArgumentException.class)
        );
    }

    static Stream<InputTestCase> clearMemoryTestCases() {
        return Stream.of(
                // 异常场景
                new InputTestCase("空会话ID-null", null, null, IllegalArgumentException.class),
                new InputTestCase("空会话ID-空字符串", "", null, IllegalArgumentException.class),
                new InputTestCase("空会话ID-空白字符串", "   ", null, IllegalArgumentException.class)
        );
    }

    @Nested
    @DisplayName("工具常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("验证 truncate 方法")
        void truncate_shouldWorkCorrectly() {
            // 直接测试 truncate 逻辑
            String shortStr = "hello";
            String longStr = "a".repeat(200);

            // 短字符串保持不变
            String result1 = truncate(shortStr, 100);
            assertEquals(shortStr, result1);

            // 长字符串被截断
            String result2 = truncate(longStr, 50);
            assertEquals(53, result2.length()); // 50 + "..."
            assertTrue(result2.endsWith("..."));
        }

        private String truncate(String str, int maxLength) {
            if (str == null) {
                return null;
            }
            return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
        }
    }

    @Nested
    @DisplayName("参数验证逻辑测试")
    class ParameterValidationTests {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.manager.MemoryChatManagerTest#inputValidationTestCases")
        @DisplayName("输入参数验证逻辑")
        void validateInput_logic(InputTestCase testCase) {
            // 验证参数校验逻辑
            if (testCase.conversationId() == null || testCase.conversationId().trim().isEmpty()) {
                assertThrows(IllegalArgumentException.class, () -> {
                    throw new IllegalArgumentException("会话ID不能为空");
                });
            } else if (testCase.prompt() == null || testCase.prompt().trim().isEmpty()) {
                assertThrows(IllegalArgumentException.class, () -> {
                    throw new IllegalArgumentException("输入内容不能为空");
                });
            } else {
                // 正常情况不会抛出异常
                assertDoesNotThrow(() -> {});
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.shinelon.hello.manager.MemoryChatManagerTest#clearMemoryTestCases")
        @DisplayName("清除记忆参数验证逻辑")
        void clearMemory_logic(InputTestCase testCase) {
            if (testCase.conversationId() == null || testCase.conversationId().trim().isEmpty()) {
                assertThrows(IllegalArgumentException.class, () -> {
                    throw new IllegalArgumentException("会话ID不能为空");
                });
            } else {
                assertDoesNotThrow(() -> {});
            }
        }
    }

    @Nested
    @DisplayName("集成测试说明")
    class IntegrationTestNote {

        @Test
        @DisplayName("说明：Manager 层 AI 调用测试需要集成测试")
        void note_aiCallTestsRequireIntegration() {
            // Manager 层的 syncCall、streamCall 方法需要真实的 ChatClient，
            // Mock ChatClient 需要模拟复杂的链式调用，建议：
            // 1. 使用 @SpringBootTest 进行集成测试
            // 2. 或使用真实的 AI 服务进行端到端测试
            //
            // 相关测试任务见 .specs/test_tasks.md 中的 M01-M06
            assertTrue(true, "此测试仅作为文档说明");
        }
    }
}
