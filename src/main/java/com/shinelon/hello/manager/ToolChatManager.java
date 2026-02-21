package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import com.shinelon.hello.tool.CalculatorTool;
import com.shinelon.hello.tool.DateTimeTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * Tool Calling Manager
 * 封装带工具调用的对话能力
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final DateTimeTool dateTimeTool;
    private final CalculatorTool calculatorTool;

    private ChatClient chatClient;

    /**
     * 工具名称常量
     */
    public static final String TOOL_DATETIME = "datetime";
    public static final String TOOL_CALCULATOR = "calculator";

    /**
     * 可用工具映射
     */
    private final Map<String, Object> toolMap = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        this.chatClient = chatClientBuilder.build();

        // 注册工具
        toolMap.put(TOOL_DATETIME, dateTimeTool);
        toolMap.put(TOOL_CALCULATOR, calculatorTool);
    }

    /**
     * 同步调用（带工具）
     *
     * @param prompt       用户输入
     * @param enabledTools 启用的工具列表，为空则使用全部
     * @return AI回复
     */
    public String syncCall(String prompt, List<String> enabledTools) {
        validatePrompt(prompt);

        Object[] tools = resolveTools(enabledTools);
        log.debug("Tool sync call with {} tools: {}", tools.length, enabledTools);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .tools(tools)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call tool chat: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e);
        }
    }

    /**
     * 流式调用（带工具）
     *
     * @param prompt       用户输入
     * @param enabledTools 启用的工具列表，为空则使用全部
     * @return AI回复流
     */
    public Flux<String> streamCall(String prompt, List<String> enabledTools) {
        validatePrompt(prompt);

        Object[] tools = resolveTools(enabledTools);
        log.debug("Tool stream call with {} tools: {}", tools.length, enabledTools);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .tools(tools)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("Failed to stream call tool chat: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "AI服务调用失败", e));
        }
    }

    /**
     * 获取可用的工具列表
     *
     * @return 工具名称列表
     */
    public List<String> getAvailableTools() {
        List<String> tools = new ArrayList<>(toolMap.size());
        tools.addAll(toolMap.keySet());
        return tools;
    }

    /**
     * 解析工具列表
     *
     * @param enabledTools 启用的工具名称列表
     * @return 工具对象数组
     */
    private Object[] resolveTools(List<String> enabledTools) {
        if (enabledTools == null || enabledTools.isEmpty()) {
            // 使用全部工具
            return toolMap.values().toArray();
        }

        List<Object> tools = new ArrayList<>(Math.min(enabledTools.size(), toolMap.size()));
        for (String toolName : enabledTools) {
            Object tool = toolMap.get(toolName.toLowerCase());
            if (tool != null) {
                tools.add(tool);
            } else {
                log.warn("Unknown tool: {}", toolName);
            }
        }

        if (tools.isEmpty()) {
            // 如果没有匹配的工具，使用全部
            return toolMap.values().toArray();
        }

        return tools.toArray();
    }

    /**
     * 验证输入
     */
    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("输入内容不能为空");
        }
    }
}
