package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.tool.ApprovalSubmitTool;
import com.shinelon.hello.tool.PermissionRoleQueryTool;
import com.shinelon.hello.tool.RolePermissionQueryTool;
import com.shinelon.hello.tool.UserQueryTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * RBAC对话管理器
 * 管理RBAC相关的AI对话，集成工具调用
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final UserQueryTool userQueryTool;
    private final RolePermissionQueryTool rolePermissionQueryTool;
    private final PermissionRoleQueryTool permissionRoleQueryTool;
    private final ApprovalSubmitTool approvalSubmitTool;

    private ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
        你是一个专业的RBAC（基于角色的访问控制）系统助手。
        
        你可以帮助用户：
        1. 查询用户信息：根据手机号查询用户的详细信息和权限
        2. 查询角色权限：查询某个角色拥有的所有权限
        3. 查询权限角色：查询拥有某个权限的所有角色
        4. 提交审批：帮助用户提交角色或权限申请的审批请求
        
        当用户询问相关问题时，请使用提供的工具来获取准确信息，然后基于查询结果回答用户。
        如果用户只是打招呼，请友好回应。
        回答要简洁、专业、准确。
        """;

    @PostConstruct
    public void init() {
        log.info("[RbacChatManager] 初始化开始，注册工具...");
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(userQueryTool)
                .defaultTools(rolePermissionQueryTool)
                .defaultTools(permissionRoleQueryTool)
                .defaultTools(approvalSubmitTool)
                .build();
        log.info("[RbacChatManager] 初始化完成，已注册4个工具");
    }

    /**
     * 与AI进行RBAC对话
     *
     * @param prompt 用户输入
     * @return AI回复
     */
    public String chat(String prompt) {
        validatePrompt(prompt);

        log.info("[chat] RBAC对话开始, prompt={}", truncate(prompt, 100));
        long startTime = System.currentTimeMillis();

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[chat] RBAC对话成功, 耗时={}ms, 响应长度={}", costTime, response.length());
            return response;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[chat] RBAC对话失败, 耗时={}ms, error={}", costTime, e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("输入内容不能为空");
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
