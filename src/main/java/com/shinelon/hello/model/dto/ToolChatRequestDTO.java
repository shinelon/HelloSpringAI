package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 带工具的对话请求DTO
 *
 * @author shinelon
 */
@Data
public class ToolChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户消息内容（必填）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000字符")
    private String content;

    /**
     * 启用的工具列表（可选，不填使用全部工具）
     * 可选值：datetime, calculator
     */
    private List<String> enabledTools;
}
