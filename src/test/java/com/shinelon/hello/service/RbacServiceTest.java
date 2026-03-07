package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RbacChatRequestDTO;
import com.shinelon.hello.model.vo.RbacChatVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RBAC服务测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
class RbacServiceTest {

    @Autowired
    private RbacService rbacService;

    record GreetingTestCase(String name, String input) {}

    static Stream<GreetingTestCase> greetingCases() {
        return Stream.of(
                new GreetingTestCase("中文你好", "你好"),
                new GreetingTestCase("中文您好", "您好"),
                new GreetingTestCase("英文hi", "hi"),
                new GreetingTestCase("英文hello", "hello"),
                new GreetingTestCase("英文hey", "hey"),
                new GreetingTestCase("中文嗨", "嗨"),
                new GreetingTestCase("中文哈喽", "哈喽"),
                new GreetingTestCase("混合问候", "Hi，你好呀")
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("greetingCases")
    void chat_greeting_shouldReturnGreetingResponse(GreetingTestCase tc) {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content(tc.input())
                .build();

        RbacChatVO response = rbacService.chat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertNotNull(response.getCreateTime());
        assertTrue(response.getContent().contains("RBAC系统助手"));
    }

    @Test
    void chat_normalQuery_shouldCallManager() {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("查询手机号13800138000的用户信息")
                .build();

        RbacChatVO response = rbacService.chat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertNotNull(response.getCreateTime());
    }

    @Test
    void chat_nullRequest_shouldThrowException() {
        assertThrows(Exception.class, () -> rbacService.chat(null));
    }

    @Test
    void chat_emptyContent_shouldThrowException() {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("")
                .build();

        assertThrows(Exception.class, () -> rbacService.chat(request));
    }

    @Test
    void chat_blankContent_shouldThrowException() {
        RbacChatRequestDTO request = RbacChatRequestDTO.builder()
                .content("   ")
                .build();

        assertThrows(Exception.class, () -> rbacService.chat(request));
    }
}
